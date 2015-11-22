package bac.peers;

import bac.peers.Peer;
import bac.helper.Helper;
import bac.settings.Settings;
import bac.cron.Cron;

import java.util.HashMap;
import java.util.Arrays;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public final class Peers {		

   public static String MyAnnouncedAddress ="127.0.0.1:8080";

   public static final int PEER_STATE_OFFLINE = 0;	
   public static final int PEER_STATE_DISCONNECTED = 1;
   public static final int PEER_STATE_UNSETTLED = 2;
	public static final int PEER_STATE_CONNECTED = 3;		

   public static HashMap<String, Peer> peers = new HashMap<>();
   public static int PeersCounter;
   
   private static Peers instance = null;     
   
    public static Peers getInstance() {
      if(instance == null) {
         instance = new Peers();
      }
      return instance;
    }   

	public static void init(){ 
       PeersCounter=0;
       Helper.logMessage("Reset PeersCounter");
       MyAnnouncedAddress=Settings.APIhost+":"+Settings.APIport;
       Helper.logMessage("Announced Address:"+MyAnnouncedAddress);
		 for (String SeedNode : Settings.SeedNodes.split(";")) {
			 SeedNode = SeedNode.trim();
			 Peer.AddPeer(SeedNode);
       }
       Cron.AddCronThread( ConnectToPeers, 5 );
       Cron.AddCronThread( FindNewPeers, 7 );	
       Cron.AddCronThread( RescanOfflinePeers, 50 );
	}
	
	
	public static Runnable ConnectToPeers = new Runnable() { 
     public void run() {
     	
       try {     	
           Peer peer = Peer.GetRandomPeer(PEER_STATE_DISCONNECTED);
			  if (peer != null) {								
				 peer.PeerConnect();								
			  }
           peer = Peer.GetRandomPeer(PEER_STATE_UNSETTLED);
			  if (peer != null) {
			  	  if ((peer.ConnectionTimestamp != 0) && (( Helper.getEpochTimestamp() - peer.ConnectionTimestamp ) > (Settings.BlockTime/20) )) {
                 peer.PeerState = PEER_STATE_CONNECTED;
					  Helper.logMessage("Peer connected."+peer.PeerAnnouncedAddress);			  	  
			  	  }								
				 								
			  }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (ConnectToPeers) error.");		 
		 }	  
     }
   };
			  
	public static Runnable RescanOfflinePeers = new Runnable() { 
     public void run() {
     	
       try {     	
           Peer peer = Peer.GetRandomPeer(PEER_STATE_OFFLINE);
			  if (peer != null) {								
				 peer.PeerState = PEER_STATE_DISCONNECTED;								
			  }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (RescanOfflinePeers) error.");		 
		 }	  
     }
   };
   
   
	public static Runnable FindNewPeers = new Runnable() { 
     public void run() {
       try {     	
           Peer peer = Peer.GetRandomPeer(PEER_STATE_CONNECTED);
			  if (peer != null) {								
				 JSONObject request = new JSONObject();
			    request.put("requestType", "GetPeers");						
			    request.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");
			    JSONObject response = peer.SendJsonQueryToPeer(request);							
             JSONArray PeersList = (JSONArray)response.get("PeersList");
				 for (int i = 0; i < PeersList.size(); i++) {								
					 String announcedAddress = ((String)PeersList.get(i)).trim(); 
					 if (announcedAddress.length() > 0) {										
						 Peer.AddPeer(announcedAddress);										
					 }									
				 }
			  }
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (FindNewPeers) error.");		 
		 }	  

     }
   };
   
   public void SendToAllPeers(JSONObject request) {
			
			Peer[] ListOfPeers;
			synchronized (peers) {				
				ListOfPeers = peers.values().toArray(new Peer[0]);				
			}
			
			for (Peer peer : ListOfPeers) {
				if (peer.PeerState == PEER_STATE_CONNECTED) {	
				   request.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");				
					peer.SendJsonQueryToPeer(request);					
				}				
			}			
	}


}
