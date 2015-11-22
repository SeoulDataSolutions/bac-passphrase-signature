package bac.blockchain;

import bac.helper.Helper;
import bac.settings.Settings;
import bac.blockchain.Blockchain;
import bac.blockchain.ForgeBlock;
import bac.crypto.Crypto;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.cron.Cron;
import bac.transaction.Transactions;
import bac.transaction.Transaction;

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

    private static HashMap<Integer, ForgeBlock> forgeblocks = new HashMap<>();

    public static void init(){ 
       Cron.AddCronThread( SynchronizeForgeData, 5 );	
	 }
	
    public static ForgeBlock GetForgeBlock( int ForgeBlockState ) {

       int EpochTimestamp = Helper.getEpochTimestamp();       	        	 	       
	    int BeginForgeTimestamp = ( EpochTimestamp / Settings.BlockTime ) * Settings.BlockTime; 
       ForgeBlock forgeblock = null;
    	
       switch ( ForgeBlockState ) {
	         case ForgeBlock.FORGEBLOCK_WAIT : {
		         forgeblock = forgeblocks.get(BeginForgeTimestamp);         
		         if (forgeblock == null) {
		           forgeblock = new ForgeBlock();
		       	  forgeblock.BeginForgeTimestamp = BeginForgeTimestamp;      
			        forgeblock.ForgeBlockState = forgeblock.FORGEBLOCK_WAIT;        	   	  
		       	  forgeblocks.put(BeginForgeTimestamp,forgeblock);       	  	
		         } 
	         } break;
	         case ForgeBlock.FORGEBLOCK_FORGING : {
              forgeblock = forgeblocks.get(BeginForgeTimestamp-Settings.BlockTime);         
              if (forgeblock != null) {
         	    if ( forgeblock.ForgeBlockState == forgeblock.FORGEBLOCK_WAIT ) {
         	       forgeblock.ForgeBlockState = forgeblock.FORGEBLOCK_FORGING;
         	    }
         	  }
	         } break;	
	         case ForgeBlock.FORGEBLOCK_CLOSED : {
	         } break;              
       }
       
       return forgeblock;
    }	
	
		
	public static Runnable SynchronizeForgeData = new Runnable() { 
     public void run() {
     	
       try {
	       int EpochTimestamp = Helper.getEpochTimestamp();       	        	 	       
		    int BeginForgeTimestamp = ( EpochTimestamp / Settings.BlockTime ) * Settings.BlockTime; 
       	
         // Add new transactions to new Forgeblock       	
       	
	      ForgeBlock forgeblock = GetForgeBlock(ForgeBlock.FORGEBLOCK_WAIT); 
       	List<Transaction> sortedTransactions;             		  
			synchronized (Transactions.UnconfirmedTransactions) {
	    	    sortedTransactions = new ArrayList<Transaction>(Transactions.UnconfirmedTransactions.values());
	    	}   
	    	Collections.sort(sortedTransactions);
	    	forgeblock.UnconfirmedTransactions = new JSONArray();
			for (Transaction transaction : sortedTransactions ) {
				
				if ( transaction.GetTimestamp() < ( BeginForgeTimestamp + ( ( Settings.BlockTime / 100 ) * 90 ) )) {
              forgeblock.UnconfirmedTransactions.add(transaction.GetTransaction());				
				}
	      }
         
         // Remove expired/closed forgeblocks
         for(Iterator<Map.Entry<Integer, ForgeBlock>> fblock = forgeblocks.entrySet().iterator(); fblock.hasNext(); ) {
             Map.Entry<Integer, ForgeBlock> forgeb = fblock.next();
             if ((forgeb.getValue().BeginForgeTimestamp < ( BeginForgeTimestamp - Settings.BlockTime )) || 
                     (forgeb.getValue().ForgeBlockState == ForgeBlock.FORGEBLOCK_CLOSED )){
                fblock.remove();
             }
         }            
         
         
         forgeblock = GetForgeBlock(ForgeBlock.FORGEBLOCK_FORGING);
         if (forgeblock != null) {
         		forgeblock.SendToAllPeersFBID();
         		if (forgeblock.SyncPeerFBIDs()) {
         		  forgeblock.GenerateNewBlock();
         		} else {
         		   Helper.logMessage("Unclosed forgeblock GetTransactionListString(): "+forgeblock.GetTransactionListString());
         		}
                        	
         }                   
                  
       	 
		 } catch (Exception e) { 
           Helper.logMessage("Cront task (NodeForgeSignatureExchange) error. "+e.toString());		 
		 }	  
     }
   };

}

