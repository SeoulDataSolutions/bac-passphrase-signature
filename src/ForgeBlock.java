package bac.blockchain;

import bac.helper.Helper;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.settings.Settings;
import bac.crypto.Crypto;

import java.math.BigInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.security.MessageDigest;

public class ForgeBlock {
	
	    public static final int FORGEBLOCK_WAIT = 0;
       public static final int FORGEBLOCK_FORGING = 1;	
       public static final int FORGEBLOCK_CLOSED = 2;	
	
	    int ForgeBlockState;
	    int BeginForgeTimestamp;
	    String PreviousBlockID;
       
                        
       JSONArray UnconfirmedTransactions;
       
       class PeerFB {
          String  FBID;
          String  FBSign;  
          String  PubKey;   
          String  Announcement;
       }
       
       private HashMap<String, String> SentFBIDs = new HashMap<>();       
       private HashMap<String, PeerFB> PeerFBIDs = new HashMap<>();
       
       String GetTransactionListString() {
       
          String TransactionListString ="";   
          Iterator transactions = UnconfirmedTransactions.iterator();

          while (transactions.hasNext()) {
             JSONObject transaction = (JSONObject) transactions.next();
             TransactionListString += ((String)transaction.get("signature")).substring(0,2);            
          }

         return TransactionListString;
          
       }

       public JSONObject GetForgeBlock() {
			
			JSONObject forgeblock = new JSONObject();
			
			forgeblock.put("PreviousBlockID", PreviousBlockID);
			forgeblock.put("BeginForgeTimestamp", BeginForgeTimestamp);
			forgeblock.put("UnconfirmedTransactions", UnconfirmedTransactions);
						
			return forgeblock;			
		 }
       
       String GetForgeBlockID() {
      	try {
             return Helper.Base58encode(MessageDigest.getInstance("SHA-256").digest(GetForgeBlock().toString().getBytes("UTF-8")));
          } catch (Exception e) {
             return null;		  
		  }      
      }           
       
       
     String GetForgeBlockSignature() {
      	try {
              return Helper.Base58encode(Crypto.sign(Helper.Base58decode(GetForgeBlockID()),Settings.NodeSecretPhrase));
          } catch (Exception e) {
             return null;		  
		  }      
      }  
     
     void SendToAllPeersFBID() {
   	  	 
   	 String ForgeFBID = GetForgeBlockID();
   	   	 
       JSONObject peerRequest = new JSONObject();
		 peerRequest.put("requestType", "NewFBID");
		 peerRequest.put("AnnouncedAddress", Peers.MyAnnouncedAddress);
		 peerRequest.put("ForgeFBID", ForgeFBID );
		 peerRequest.put("ForgeFBSign", GetForgeBlockSignature() );
		 peerRequest.put("NodePubKey", Settings.NodePublicKey );
		 
		 Peer[] ListOfPeers;
			synchronized (Peers.peers) {				
				ListOfPeers = Peers.peers.values().toArray(new Peer[0]);				
			}
			for (Peer peer : ListOfPeers) {
				if (peer.PeerState == Peers.PEER_STATE_CONNECTED) {	
				   String SentFBID = SentFBIDs.get(Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress));
				   if (( SentFBID == null ) || ( !(SentFBID.equals( ForgeFBID )) )) { 
                        peerRequest.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");				
								JSONObject response = peer.SendJsonQueryToPeer(peerRequest);
													
								if ((response.get("Accepted") != null) && ((Boolean)response.get("Accepted") == true)) {						
								  SentFBIDs.put( Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress), ForgeFBID );
								  Helper.logMessage("Accepted "+peer.PeerAnnouncedAddress);
								}

				   } 
				}				
			} // for									  	
   }
   
   public boolean NewFBID(String PeerFBID, String PeerFBSign , String PubKey , String PeerAnnouncement) {
      
	   	Peer peer = Peers.peers.get(Helper.GetAnnouncementHost(PeerAnnouncement));
	   	if ( peer != null ) {  	
	   	  PeerFB peerfb = new PeerFB();
	   	  peerfb.FBID = PeerFBID;
	   	  peerfb.FBSign = PeerFBSign;
	   	  peerfb.PubKey = PubKey; 
	   	  peerfb.Announcement = peer.PeerAnnouncedAddress;
	   	  if (!VerifyPeerFB(peerfb)) { 
	   	    return false;
	   	  }  
	   	  PeerFBIDs.put( Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress), peerfb );
	   	  return true;   	  
	   	}
      return false;    
   }
   
   
   public boolean VerifyPeerFB(PeerFB peerfb) {
     try { 	
	     if (Crypto.verify(Helper.Base58decode(peerfb.FBSign), Helper.Base58decode(peerfb.FBID), Helper.Base58decode(peerfb.PubKey))) {
	        return true;
	     }
     } catch (Exception e) {}	
     return false;   
   }
          
                
        

   
   
   public synchronized Boolean SyncPeerFBIDs() {
		 Peer[] ListOfPeers;
			synchronized (Peers.peers) {				
				ListOfPeers = Peers.peers.values().toArray(new Peer[0]);				
			}
			int CounterSyncedFBID = 0;
			int CounterPeers = 0;
			for (Peer peer : ListOfPeers) {
				if (peer.PeerState == Peers.PEER_STATE_CONNECTED) {
					CounterPeers++;
					synchronized (PeerFBIDs) {
                  PeerFB peerfb = new PeerFB();						
	               peerfb = PeerFBIDs.get(Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress));
	               
	               
	               if ((peerfb != null) && (( peerfb.FBID != null ))) {
	                 if ( !(peerfb.FBID.equals( GetForgeBlockID() )) ) {
                        // Maybe next time...	                 
	                 }  else {
	               	 CounterSyncedFBID++;              
	                 }

	               } 
	               
               }
					
				}
						
         } // for 
        if ( (float)CounterSyncedFBID > (float)CounterPeers * 0.8 ) {      	      		
        return true;      
      }   
      return false; 
   }
   
   public void GenerateNewBlock() {
 
       if (GetBlockForgerPeerFB(GetForgeBlockSignature())) {
          Helper.logMessage("New block.");
       } 
       ForgeBlockState = FORGEBLOCK_CLOSED;
   }
   
   public Boolean GetBlockForgerPeerFB(String OwnForgeBlockSignature ) {
   	
   	   PeerFB BlockForgerPeerFB = null;
         Peer[] ListOfPeers;
			synchronized (Peers.peers) {				
				ListOfPeers = Peers.peers.values().toArray(new Peer[0]);				
			}
			String ForgeBlockID = GetForgeBlockID();
			for (Peer peer : ListOfPeers) {
				if (peer.PeerState == Peers.PEER_STATE_CONNECTED) {
					synchronized (PeerFBIDs) {
                  PeerFB peerfb = new PeerFB();						
	               peerfb = PeerFBIDs.get(Helper.GetAnnouncementHost( peer.PeerAnnouncedAddress));
	               if ((peerfb != null) && (( peerfb.FBID != null ) && ( (peerfb.FBID.equals( ForgeBlockID )) ))) {
	               	  if (BlockForgerPeerFB == null) {
	               	     BlockForgerPeerFB = peerfb;
	               	  }
	                    BigInteger ThisFBvalue = Helper.Base58ToBigInteger(peerfb.FBSign);
	                    BigInteger PrevFBvalue = Helper.Base58ToBigInteger(BlockForgerPeerFB.FBSign);
	                    if ( ThisFBvalue.compareTo(PrevFBvalue) < 0 ) {
	                       BlockForgerPeerFB = peerfb;
	                    }
	               } 
               }
					
				}
						
         } // for 
         
       if ( BlockForgerPeerFB != null ) {
	                    if ( Helper.Base58ToBigInteger(OwnForgeBlockSignature).compareTo(Helper.Base58ToBigInteger(BlockForgerPeerFB.FBSign)) <= 0 ) {
	                      return true;   
	                    }       
       }
       
       return false;  
  }
  
     
}


