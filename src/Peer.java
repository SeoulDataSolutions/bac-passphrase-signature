package bac.peers;

import bac.settings.Settings;
import bac.peers.Peers;
import bac.helper.Helper;
import bac.api.APIServlet;
import bac.Bac;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

import org.json.simple.JSONObject;


public class Peer  {

	public int PeerID;
	public int PeerState;
	public int ConnectionTimestamp;
	
   public String PeerAnnouncedAddress;

   Peer(String PeerAnnouncedAddress) {			
			this.PeerAnnouncedAddress = PeerAnnouncedAddress;						
	}

   public static Peer AddPeer(String announcedAddress) {
						
			synchronized (Peers.peers) {				
				if ( announcedAddress.equals(Peers.MyAnnouncedAddress) ) {					
					return null;					
				}				
				Peer peer = Peers.peers.get(Helper.GetAnnouncementHost(announcedAddress));
				if (peer == null) {					
					peer = new Peer(announcedAddress);
					peer.PeerID = ++Peers.PeersCounter;
					peer.PeerState = Peers.PEER_STATE_DISCONNECTED;
					peer.ConnectionTimestamp = 0;
					Peers.peers.put(Helper.GetAnnouncementHost(announcedAddress),peer);					
				}				
				return peer;				
			}			
		}


      void RemovePeer() {
      	
			synchronized (Peers.peers) {					
				for (Map.Entry<String, Peer> PeerEntry : Peers.peers.entrySet()) {				
					if (PeerEntry.getValue() == this) {					
						Peers.peers.remove(PeerEntry.getKey());					
						break;					
					}				
				}				
			}				
		}
		
		public static Peer GetRandomPeer( int PeerStateFilter ) {
			
			synchronized (Peers.peers) {
            Collection<Peer> PeersList = ((HashMap<String, Peer>)Peers.peers.clone()).values();
				Iterator<Peer> FilteredPeers = PeersList.iterator();
				while (FilteredPeers.hasNext()) {					
					Peer peer = FilteredPeers.next();
					if ( peer.PeerState != PeerStateFilter ) {						
						FilteredPeers.remove();						
					}				
				}
				if (PeersList.size() > 0) {
				  return (Peer)Helper.getRandomObject(PeersList);
				} else return null;
			}			
		}
		
      public JSONObject SendJsonQueryToPeer(JSONObject request) {
      	APIServlet listenner = new APIServlet();         
         JSONObject response = listenner.SendJsonQuery(request);
         if (response != null) {
         	// Helper.logMessage("Peer response: "+response.toString());
         	JSONObject Data = (JSONObject) response.get("Data");
         	if (Data != null) {
              return Data;
            }  
         } 
         PeerDisconnect();
         return null;   
      }		
		
		void PeerConnect() {

        try {		
				JSONObject request = new JSONObject();
				request.put("requestType", "GetInfo");						
				request.put("serverURL", "http://"+PeerAnnouncedAddress+"/api");
				request.put("AnnouncedAddress",Peers.MyAnnouncedAddress);
	
	         JSONObject response = SendJsonQueryToPeer(request);
				if (response != null) {			  
					  String ConnectedAnnouncedAddress = (String) response.get("AnnouncedAddress");
					  if ( ConnectedAnnouncedAddress.length() > 0 ) {
					      PeerState = Peers.PEER_STATE_UNSETTLED;
					      ConnectionTimestamp = Helper.getEpochTimestamp();
					      Helper.logMessage("Peer connected.(UNSETTLED)"+ConnectedAnnouncedAddress); 				  
					  } else {
					      Helper.logMessage("Bad or missing AnnouncedAddress."+response.toString()); 
					  }    
	         } else {
	            // Helper.logMessage("Peer connection fail. "+request.toString());
	         }     	
		  } catch (Exception e) { 
		     Helper.logMessage("Peer connection error.");
		  }		 		 								
		}
		
		void PeerDisconnect() {			
			if ( PeerState == Peers.PEER_STATE_DISCONNECTED ) {
				Helper.logMessage("Peer ("+PeerAnnouncedAddress+") offline.");
				PeerState = Peers.PEER_STATE_OFFLINE;						
			} else {
				Helper.logMessage("Peer ("+PeerAnnouncedAddress+") disconnected.");
				PeerState = Peers.PEER_STATE_DISCONNECTED;			
			} 
			ConnectionTimestamp = 0;
		}
      					
}
