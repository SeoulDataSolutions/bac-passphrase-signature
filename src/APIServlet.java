package bac.api;

import bac.helper.Helper;
import bac.crypto.Crypto;
import bac.settings.Settings;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.io.IOException;

import javax.servlet.http.*;
import javax.servlet.ServletException;
 

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

        switch ( (String) ajaxRequest.get("requestType") ) {					
				case "GetAddress": {    
				  AjaxResponse = AjaxGetAddress(ajaxRequest);   
            }
				case "GetInfo": {    
				  AjaxResponse = AjaxGetInfo(ajaxRequest);   
            }

        }                       
                
        response.setContentType("text");
        PrintWriter ServletOutputStream = response.getWriter();        
        ServletOutputStream.print(AjaxResponse.toString());
    }
    

    public void destroy() {
        // do nothing.
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
       
		 try {       
		       response.put("timestamp",System.currentTimeMillis());
		       response.put("AnnouncedAddress", Settings.MyAnnouncedAddress);
		       response.put("Version", Settings.VERSION);
		       response.put("requestType", ajaxRequest.get("requestType"));
		 } catch (Exception e) {
				     Helper.logMessage("Response error. (AjaxGetAddress)");
				     response.put("timestamp",System.currentTimeMillis());
				     response.put("error",1);
		 }              
      
       return response;
    }
}