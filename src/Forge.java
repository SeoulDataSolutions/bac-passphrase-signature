package bac.blockchain;

import bac.helper.Helper;
import bac.blockchain.Blockchain;
import bac.crypto.Crypto;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.cron.Cron;

import java.util.*;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.HashMap;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Iterator;

public final class Forge {
                        
    private static Forge instance = null;     
   
    public static Forge getInstance() {
      if(instance == null) {
         instance = new Forge();
      }
      return instance;
    }                        
    
    
    public static void init(){ 
       Cron.AddCronThread( NodeForgeSignatureExchange, 5 );	
	 }
	
	
	public static Runnable NodeForgeSignatureExchange = new Runnable() { 
     public void run() {
     	
       try {
       	if ( nfsigns.size() < 1 ) { // or anyone create new block
           UpdateMyForgeSignature();
           nfsigns.put(Helper.GetAnnouncementHost(MyForgeSignature.NodeAnnouncement), MyForgeSignature);
           SendToAllPeersNFSignHash();              
         }	     	         
         if (GetNewNFSfromPeers()) {  
           SendToAllPeersNFSignHash();                           
         }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (NodeForgeSignatureExchange) error. "+e.toString());		 
		 }	  
     }
   };    
    

   static void SendToAllPeersNFSignHash() {
   	
   	 String NodeForgeSignaturesHash = Helper.Base58encode(GetNodeForgeSignaturesHash());
   	 
       JSONObject peerRequest = new JSONObject();
		 peerRequest.put("requestType", "NewNFSignHash");
		 peerRequest.put("AnnouncedAddress", Peers.MyAnnouncedAddress);
		 peerRequest.put("NodeForgeSignaturesHash", NodeForgeSignaturesHash);
		 
		 Peer[] ListOfPeers;
			synchronized (Peers.peers) {				
				ListOfPeers = Peers.peers.values().toArray(new Peer[0]);				
			}
			for (Peer peer : ListOfPeers) {
				if (peer.PeerState == Peers.PEER_STATE_CONNECTED) {	
				   String SentNodeForgeSignaturesHash = SentPeerNFSigns.get(Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress));
				   if (( SentNodeForgeSignaturesHash == null ) || ( !(SentNodeForgeSignaturesHash.equals( NodeForgeSignaturesHash )) )) { 
                        peerRequest.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");				
								JSONObject response = peer.SendJsonQueryToPeer(peerRequest);					
								if ((Boolean)response.get("Accepted") == true) {						
								  SentPeerNFSigns.put( Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress), NodeForgeSignaturesHash );
								  Helper.logMessage("Accepted "+peer.PeerAnnouncedAddress);
								}

				   } else { 
				      Helper.logMessage("No send again." + peer.PeerAnnouncedAddress );
				   }
				}				
			} // for									  
   }
   
   
   static Boolean GetNewNFSfromPeers() {
   	
   	 Boolean  NfsTableChanged = false;
   	 if ( PeerNFSigns.size() < 1 ) {
   	    NfsTableChanged = true; // Need broadcasting 
   	 } else {  
           Peer[] ListOfPeers;
	        synchronized (Peers.peers) {				
		         ListOfPeers = Peers.peers.values().toArray(new Peer[0]);				
	        }
	        for (Peer peer : ListOfPeers) {
		         if (peer.PeerState == Peers.PEER_STATE_CONNECTED) {
					   String PeerNodeForgeSignaturesHash = PeerNFSigns.get(Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress));
					   if (( PeerNodeForgeSignaturesHash != null ) && ( !(PeerNodeForgeSignaturesHash.equals( Helper.Base58encode(GetNodeForgeSignaturesHash()) )) )) {
				   	     Helper.logMessage(" Get signatures from "+peer.PeerAnnouncedAddress  );
				   	     JSONObject peerRequest = new JSONObject();
						     peerRequest.put("requestType", "GetNodeForgeSignatures");
						     peerRequest.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");
						     peerRequest.put("AnnouncedAddress", Peers.MyAnnouncedAddress);
                       JSONObject response = peer.SendJsonQueryToPeer(peerRequest);					        
					        JSONArray ForgeSignatures = (JSONArray)response.get("ForgeSignatures");
				           for (int i = 0; i < ForgeSignatures.size(); i++) {								
					           JSONObject ForgeSignatureJSON = (JSONObject)ForgeSignatures.get(i);
					           NodeForgeSignature ForgeSignature = new NodeForgeSignature();

						        ForgeSignature.ForgePublicKey = Helper.Base58decode((String)ForgeSignatureJSON.get("ForgePublicKey"));
						        ForgeSignature.ForgeSignature = Helper.Base58decode((String)ForgeSignatureJSON.get("ForgeSignature"));
						        ForgeSignature.NodeAnnouncement = (String)ForgeSignatureJSON.get("NodeAnnouncement");
						        ForgeSignature.FSvalidity = ((Long)ForgeSignatureJSON.get("FSvalidity")).intValue(); 
						        
						
						        if (!(Crypto.verify(ForgeSignature.ForgeSignature, Helper.Base58decode(Blockchain.GetLastBlock().GetBlockID()), ForgeSignature.ForgePublicKey))) {
						               Helper.logMessage("Error in ForgeSignature verify.");        
						        } else {
						        	   // More checkings needed before add
						                       nfsigns.put(Helper.GetAnnouncementHost(ForgeSignature.NodeAnnouncement), ForgeSignature);
						                       NfsTableChanged = true; // Need broadcasting 
						        }
					                       
					 
				           } // for Forgesignatures
					        
					   } 
		         	
		         	
		         }
		     }  // for Peers   	   	 	
   	 	
   	 	
   	 	
   	 }

       return NfsTableChanged;   
   }
   
   private static HashMap<String, String> PeerNFSigns = new HashMap<>();
   private static HashMap<String, String> SentPeerNFSigns = new HashMap<>();

   static public void NewNFSignHash(String NodeForgeSignaturesHash, String PeerAnnouncement) {
   	
   	
   	Peer peer = Peers.peers.get(Helper.GetAnnouncementHost(PeerAnnouncement));
   	if ( peer != null ) {  	
   	  PeerNFSigns.put( Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress), NodeForgeSignaturesHash );   	  
   	}
   	
       
   }
                        
    static class NodeForgeSignature implements Comparable<NodeForgeSignature> {
       String NodeAnnouncement;
       byte[] ForgeSignature;
       byte[] ForgePublicKey;
       int FSvalidity;
       
       public int compareTo(NodeForgeSignature compareNfsign) {

	      return this.SignToNumber().compareTo(((NodeForgeSignature) compareNfsign).SignToNumber());		
		
	    }
	    
	    BigInteger SignToNumber() {          
           return new BigInteger(1, new byte[] { ForgeSignature[0], ForgeSignature[1], ForgeSignature[2], ForgeSignature[3], 
                                                 ForgeSignature[4], ForgeSignature[5], ForgeSignature[6], ForgeSignature[7] });	    
	    }	           
       
    }
    
    private static byte[] ForgeSecret = new byte[32];
    private static NodeForgeSignature MyForgeSignature = new NodeForgeSignature(); 
 
    
    private static HashMap<String, NodeForgeSignature> nfsigns = new HashMap<>();  
            
    
    static void UpdateMyForgeSignature() {
    	             	                 	              
        new Random().nextBytes(ForgeSecret);        
        MyForgeSignature.ForgePublicKey = Crypto.getPublicKey(new String(ForgeSecret, StandardCharsets.UTF_8));
        MyForgeSignature.ForgeSignature = Crypto.sign(Helper.Base58decode(Blockchain.GetLastBlock().GetBlockID()),new String(ForgeSecret, StandardCharsets.UTF_8));
        if (!(Crypto.verify(MyForgeSignature.ForgeSignature, Helper.Base58decode(Blockchain.GetLastBlock().GetBlockID()), MyForgeSignature.ForgePublicKey))) {
               Helper.logMessage("Error in ForgeSignature verify.");        
        }
       MyForgeSignature.NodeAnnouncement = Peers.MyAnnouncedAddress;
       MyForgeSignature.FSvalidity = Helper.getEpochTime(System.currentTimeMillis())+120; 

    }      
    
    public static JSONArray GetNodeForgeSignatures() {    
           
       JSONArray NodeForgeSignatures = new JSONArray();
       Collection<NodeForgeSignature> nfsignsCollection = ((HashMap<String, NodeForgeSignature>)nfsigns.clone()).values();
       Iterator<NodeForgeSignature> it = nfsignsCollection.iterator();
       while (it.hasNext()) {
        NodeForgeSignature nfsign = it.next();
        JSONObject  nfsignJSON = new JSONObject();
        nfsignJSON.put("NodeAnnouncement", nfsign.NodeAnnouncement);
        nfsignJSON.put("ForgeSignature", Helper.Base58encode(nfsign.ForgeSignature));
        nfsignJSON.put("ForgePublicKey", Helper.Base58encode(nfsign.ForgePublicKey));
        nfsignJSON.put("FSvalidity", nfsign.FSvalidity);
        NodeForgeSignatures.add(nfsignJSON);
        it.remove(); 
       }    
       return NodeForgeSignatures;
    }

    static byte[] GetNodeForgeSignaturesHash() {

        try {
	    	  List<NodeForgeSignature> sortedNFS = new ArrayList<NodeForgeSignature>(nfsigns.values());
	    	  Collections.sort(sortedNFS);
	    	  MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        for (NodeForgeSignature nfs : sortedNFS ) {
	           digest.update(nfs.ForgeSignature);
	        }
	        byte[] nfsHash = digest.digest();    
           return nfsHash;
           	        	  
    	  } catch (Exception e) { }
            	  
    	  return null;
    }

    BigInteger GetNodeForgeSignaturesNumber() {

        try {
	        byte[] nfsHash = GetNodeForgeSignaturesHash();    
           return new BigInteger(1, new byte[] { nfsHash[0], nfsHash[1], nfsHash[2], nfsHash[3], 
                                                 nfsHash[4], nfsHash[5], nfsHash[6], nfsHash[7] });	        	  
    	  } catch (Exception e) { }
            	  
    	  return null;
    }

    NodeForgeSignature GetBestSignature() {
    	     BigInteger NodeForgeSignaturesNumber = GetNodeForgeSignaturesNumber();
	    	  List<NodeForgeSignature> sortedNFS = new ArrayList<NodeForgeSignature>(nfsigns.values());
	    	  Collections.sort(sortedNFS);
	    	  NodeForgeSignature prevnfs = null;
	        for (NodeForgeSignature nfs : sortedNFS ) {        		           	           
				  if( NodeForgeSignaturesNumber.compareTo(nfs.SignToNumber()) == 1 ) { 
				    return nfs; 
				  } 	
	        }
	        return sortedNFS.iterator().next();    
    }
    
    

}