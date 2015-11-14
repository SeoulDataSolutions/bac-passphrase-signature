package bac.api;

import bac.helper.Helper;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.crypto.Crypto;
import bac.settings.Settings;
import bac.transaction.Transaction;
import bac.transaction.Transactions;
import bac.blockchain.Forge;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.io.IOException;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

 
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.net.*; 
import java.net.InetSocketAddress;

public final class APIServlet extends HttpServlet {

    public void init() throws ServletException {
        // Do required initialization
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter ServletOutputStream = response.getWriter();
        ServletOutputStream.println("<h1>" + "The method specified in the Request Line is not allowed for the resource identified by the request." + "</h1>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    
      throws ServletException, IOException {     
        JSONObject ajaxRequest = new JSONObject(); 
		  try {
          ajaxRequest = (JSONObject)new JSONParser().parse(request.getReader());
			 
		  }  catch (ParseException e) {
                e.printStackTrace();
        }
			    		        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("text/plain; charset=UTF-8");
       
        JSONObject AjaxResponse = new JSONObject();

        // Helper.logMessage("API request:"+ajaxRequest.toString()+" Address:"+Helper.GetAnnouncementHost((String) ajaxRequest.get("AnnouncedAddress"))+" Remote:"+request.getRemoteAddr());         
         
         if (( ajaxRequest.get("AnnouncedAddress") == null ) || 
              (Helper.GetAnnouncementHost((String) ajaxRequest.get("AnnouncedAddress")).equals(request.getRemoteAddr()) ) ) {
        
	        switch ( (String) ajaxRequest.get("requestType") ) {					
					case "GetAddress": {    
					  AjaxResponse = AjaxGetAddress(ajaxRequest);   
	            } break;
					case "GetInfo": {    
					  AjaxResponse = AjaxGetInfo(ajaxRequest);   
	            } break;
					case "GetPeers": {    
					  AjaxResponse = AjaxGetPeers(ajaxRequest);   
	            } break;
					case "GetAllPeerDetails": {    
					  AjaxResponse = AjaxGetAllPeerDetails(ajaxRequest);   
	            } break;
					case "GetNodeForgeSignatures": {    
					  AjaxResponse = AjaxGetNodeForgeSignatures(ajaxRequest);   
	            } break;
	            
	            
	            
	            // Hidden requests
					case "ProcessTransactions": {    
					  AjaxResponse = AjaxProcessTransactions(ajaxRequest);   
	            } break;
					case "GetUnconfirmedTransactions": {    
					  AjaxResponse = AjaxGetUnconfirmedTransactions(ajaxRequest);   
	            } break;
					case "NewNFSignHash": {    
					  AjaxResponse = AjaxNewNFSignHash(ajaxRequest);   
	            } break;                        	            
	            default: {            	
					   AjaxResponse.put("error","Bad requestType.");
	            } break;
	        }                       
        } else {
         	
			   AjaxResponse.put("error","Bad announced address.");        
        }                
        response.setContentType("text");
        PrintWriter ServletOutputStream = response.getWriter();  
        // Helper.logMessage("Response:"+AjaxResponse.toString());  
        AjaxResponse.put("timestamp",Helper.getEpochTimestamp());    
        ServletOutputStream.print(AjaxResponse.toString());
    }
    

    public void destroy() {
        // do nothing.
    }
    
    
	 public synchronized JSONObject SendJsonQuery(JSONObject request) {
	    
	    JSONObject JSONresponse = new JSONObject();
	    
       // Helper.logMessage("SendJsonQuery:"+request.toString());	    
	    
	    try {
	    	
		    	HttpClient client = new HttpClient();
		    	request.put("timestamp",Helper.getEpochTimestamp());
		    	client.setBindAddress(new InetSocketAddress( InetAddress.getByName(Settings.APIhost) , 0 )); 
	         client.start();
		    	ContentResponse response = client.POST((String)request.get("serverURL"))
	        .content(new StringContentProvider(request.toString()) , "application/json; charset=UTF-8")
	        .send();                       
	        client.stop();
	        if ( response.getStatus() == HttpURLConnection.HTTP_OK ) {
						    try {
						          JSONresponse.put("Data",(JSONObject)new JSONParser().parse(response.getContentAsString()));
						    } catch (Exception e) {
						          JSONresponse.put("Error","Failed parsing returned JSON object.");
						    }		           
		     }             	
	    } catch (Exception e) {
	             JSONresponse.put("Error","Communication error.");
	    }
	        	     
	    return JSONresponse;
	 }    
    
    private JSONObject AjaxGetAddress( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       
       byte[] PublicKey = new byte[32];                    
       PublicKey = Crypto.getPublicKey((String)ajaxRequest.get("secretPhrase"));
		 try {       
		       response.put("PublicKey", Helper.Base58encode((byte[]) PublicKey));
		       response.put("BAC Address", Helper.PublicKeyToAddress((byte[]) PublicKey));
		       response.put("secretPhrase", ajaxRequest.get("secretPhrase"));
		       response.put("requestType", ajaxRequest.get("requestType"));
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAddress)");
				     response.put("error",1);
		 }              
      
       return response;
    }
    
    private JSONObject AjaxGetInfo( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       
       if ( ajaxRequest.get("AnnouncedAddress").toString().length() > 0 ) {
           Peer.AddPeer((String)ajaxRequest.get("AnnouncedAddress"));       
       }
       
		 try {       
		       response.put("AnnouncedAddress", Peers.MyAnnouncedAddress);
		       response.put("Version", Settings.VERSION);
		       response.put("requestType", ajaxRequest.get("requestType"));
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAddress)");
				     response.put("error",1);
		 }              
      
       return response;
    }
    
    private JSONObject AjaxGetPeers( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       JSONArray PeersList = new JSONArray();
       Set<String> PeersAnnouncements;       
       
		 try {       
				 synchronized (Peers.peers) {					
				    PeersAnnouncements = ((HashMap<String, Peer>)Peers.peers.clone()).keySet();					
				 }
			 	 for (Map.Entry<String, Peer> PeerEntry : Peers.peers.entrySet()) {				
					 Peer peer = PeerEntry.getValue();					 
					 PeersList.add(peer.PeerAnnouncedAddress);					
				 }
		       response.put("PeersList", PeersList);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetPeers)");
				     response.put("error",1);
		 }              
       return response;
    }    
        
        
    private JSONObject AjaxGetAllPeerDetails( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       JSONArray PeersList = new JSONArray();      
       
		 try { 
				 synchronized (Peers.peers) {
				 	 for (Map.Entry<String, Peer> PeerEntry : Peers.peers.entrySet()) {				
						 Peer peer = PeerEntry.getValue();
						 PeersList.add( "ID:"+peer.PeerID+" Announce Address:"+peer.PeerAnnouncedAddress+" State:"+peer.PeerState );					
					 }
				 }	 		 					
		       response.put("PeersList", PeersList);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAllPeerDetails)");
				     response.put("error",1);
		 }              
      
       return response;
    }
    
    private JSONObject AjaxProcessTransactions( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       
		 try { 				 
				 Transactions.getInstance().processTransactions((JSONArray)ajaxRequest.get("ValidatedTransactions"), false );
		       response.put("Accepted", true);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxProcessTransactions)");
				     response.put("error",1);
		 }              
      
       return response;
    }
    
    private JSONObject AjaxGetUnconfirmedTransactions( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       JSONArray UnconfirmedTransactions = new JSONArray();      
       
		 try { 
				 synchronized (Transactions.UnconfirmedTransactions) {
				 	 for (Map.Entry<String, Transaction> UnconfirmedTransactionsEntry : Transactions.UnconfirmedTransactions.entrySet()) {				
						 UnconfirmedTransactions.add(UnconfirmedTransactionsEntry.getValue().GetTransaction());					
					 }
				 }	 		 					
		       response.put("UnconfirmedTransactions", UnconfirmedTransactions);				 

		       response.put("Accepted", true);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxProcessTransactions)");
				     response.put("error",1);
		 }              
      
       return response;
    }
 
     private JSONObject AjaxNewNFSignHash( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       
		 try { 				 
				 Forge.getInstance().NewNFSignHash((String)ajaxRequest.get("NodeForgeSignaturesHash"), (String)ajaxRequest.get("AnnouncedAddress") );
		       response.put("Accepted", true);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxNewNFSignHash)");
				     response.put("error",1);
		 }              
      
       return response;
    }

                    
private JSONObject AjaxGetNodeForgeSignatures( JSONObject ajaxRequest ) {
    	
       Helper.logMessage("Accept request:"+ajaxRequest.toString());    	
       JSONObject response = new JSONObject();
		 try {       
		       response.put("ForgeSignatures", Forge.GetNodeForgeSignatures() );
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetNodeForgeSignatures)");
				     response.put("error",1);
		 }              
      
       return response;
    }
        
}