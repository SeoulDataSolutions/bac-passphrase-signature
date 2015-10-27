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
	
   public String PeerAnnouncedAddress;

   Peer(String PeerAnnouncedAddress) {			
			this.PeerAnnouncedAddress = PeerAnnouncedAddress;						
	}

   public static Peer AddPeer(String announcedAddress) {
						
			synchronized (Peers.peers) {				
				if ( announcedAddress.equals(Peers.MyAnnouncedAddress) ) {					
					return null;					
				}				
				Peer peer = Peers.peers.get(announcedAddress);
				if (peer == null) {					
					peer = new Peer(announcedAddress);
					peer.PeerID = ++Peers.PeersCounter;
					peer.PeerState = Peers.PEER_STATE_DISCONNECTED;
					Peers.peers.put(announcedAddress,peer);					
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
		
		static Peer GetRandomPeer( int PeerStateFilter ) {
			
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
		
      JSONObject SendJsonQueryToPeer(JSONObject request) {
         JSONObject response = APIServlet.SendJsonQuery(request);
         if (response != null) {
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
					      PeerState = Peers.PEER_STATE_CONNECTED;
					      Helper.logMessage("Peer connected. "+response.toString()); 				  
					  } else {
					      Helper.logMessage("Bad or missing AnnouncedAddress."+response.toString()); 
					  }    
	         } else {
	            Helper.logMessage("Peer connection fail. "+request.toString());
	         }     	
		  } catch (Exception e) { 
		     Helper.logMessage("Peer connection error.");
		  }		 		 								
		}
		
		void PeerDisconnect() {
			PeerState = Peers.PEER_STATE_DISCONNECTED;
		}
      					
}
