package bac.api;

import bac.helper.Helper;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.crypto.Crypto;
import bac.settings.Settings;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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
      	Helper.logMessage("API request received.");      
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

        Helper.logMessage("API request:"+ajaxRequest.toString());

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
            default: {            	
            	AjaxResponse.put("timestamp",System.currentTimeMillis());
				   AjaxResponse.put("error","Bad requestType.");
            } break;
        }                       
                
        response.setContentType("text");
        PrintWriter ServletOutputStream = response.getWriter();  
        Helper.logMessage("Response:"+AjaxResponse.toString());      
        ServletOutputStream.print(AjaxResponse.toString());
    }
    

    public void destroy() {
        // do nothing.
    }
    
    
	 public static JSONObject SendJsonQuery(JSONObject request) {
	    
	    JSONObject JSONresponse = new JSONObject();
	    
       Helper.logMessage("SendJsonQuery:"+request.toString());	    
	    
	    try {
					URL object=new URL((String)request.get("serverURL"));
					HttpURLConnection con = (HttpURLConnection) object.openConnection();
					con.setDoOutput(true);
					con.setDoInput(true);
					con.setRequestProperty("Content-Type", "application/json");
					con.setRequestProperty("Accept", "application/json");
					con.setRequestMethod("POST");
					OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
					wr.write(request.toString());
					wr.flush();
					StringBuilder sb = new StringBuilder();  
					int HttpResult = con.getResponseCode(); 
					if ( HttpResult == HttpURLConnection.HTTP_OK ) {
					    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
					    String line = null;  
					    while ((line = br.readLine()) != null) {  
					        sb.append(line + "\n");  
					    }  
					    br.close();  
					    try {
					          JSONresponse.put("Data",(JSONObject)new JSONParser().parse(sb.toString()));
					    } catch (Exception e) {
					          JSONresponse.put("Error","Failed parsing returned JSON object.");
					    }
	           } else {
	                     JSONresponse.put("Error","HTTP error.");  
	                  }  
	
	        } catch (IOException e) {
	             JSONresponse.put("Error","IOException error.");
	        }
	        	     
	    return JSONresponse;
	 }    
    
    private JSONObject AjaxGetAddress( JSONObject ajaxRequest ) {
    	
       JSONObject response = new JSONObject();
       
       byte[] PublicKey = new byte[32];                    
       PublicKey = Crypto.getPublicKey((String)ajaxRequest.get("secretPhrase"));
		 try {       
		       response.put("timestamp",System.currentTimeMillis());
		       response.put("PublicKey", Helper.Base58encode((byte[]) PublicKey));
		       response.put("BAC Address", Helper.PublicKeyToAddress((byte[]) PublicKey));
		       response.put("secretPhrase", ajaxRequest.get("secretPhrase"));
		       response.put("requestType", ajaxRequest.get("requestType"));
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAddress)");
				     response.put("timestamp",System.currentTimeMillis());
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
		       response.put("timestamp",System.currentTimeMillis());
		       response.put("AnnouncedAddress", Peers.MyAnnouncedAddress);
		       response.put("Version", Settings.VERSION);
		       response.put("requestType", ajaxRequest.get("requestType"));
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAddress)");
				     response.put("timestamp",System.currentTimeMillis());
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
				 for (String PeerAnnouncement : PeersAnnouncements ) {					
					PeersList.add(PeerAnnouncement);					
				 }		
		       response.put("timestamp",System.currentTimeMillis());
		       response.put("PeersList", PeersList);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetPeers)");
				     response.put("timestamp",System.currentTimeMillis());
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
		       response.put("timestamp",System.currentTimeMillis());
		       response.put("PeersList", PeersList);
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAllPeerDetails)");
				     response.put("timestamp",System.currentTimeMillis());
				     response.put("error",1);
		 }              
      
       return response;
    }    
        
}