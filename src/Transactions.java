package bac.transaction;

import bac.helper.Helper;
import bac.crypto.Crypto;
import bac.transaction.Transaction;
import bac.cron.Cron;
import bac.peers.Peers;
import bac.peers.Peer;
import bac.account.Account;
import bac.account.Accounts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;

public final class Transactions  {


		private static Transactions instance = null;
      
		   
	   public static synchronized Transactions getInstance() {
	      if(instance == null) {
	         instance = new Transactions();
	      }
	      return instance;
	   }		   

	
		//static int TransactionCounter;
	   static HashMap<String, Transaction> transactions  = new HashMap<>();
	   public static ConcurrentHashMap<String, Transaction> UnconfirmedTransactions = new ConcurrentHashMap<>();
	   static ConcurrentHashMap<String, Transaction> DoubleSpendingTransactions = new ConcurrentHashMap<>();

      public static void init() {
        Cron.AddCronThread( RemoveExpiredTransactions, 8 );
        Cron.AddCronThread( GetUnconfirmedTransactions, 4 );
      }


     public static Runnable RemoveExpiredTransactions = new Runnable() { 
     public void run() {
     	
       try {
       	    synchronized (transactions) {					
					Iterator<Transaction> iterator = UnconfirmedTransactions.values().iterator();
               int curTime = Helper.getEpochTime(System.currentTimeMillis());					
					while (iterator.hasNext()) {
						Transaction transaction = iterator.next();
						if ( curTime > (transaction.timestamp + transaction.deadline) ) {							
							iterator.remove();
							Account account = Accounts.GetPubkeyAccount(transaction.senderPublicKey);
									synchronized (account) {										
										account.SetUnconfirmedBalance(account.GetUnconfirmedBalance() + (transaction.amount + transaction.fee) );										
									}
                  }						
               }       	    	
       	    }	
		 } catch (Exception e) { 
           Helper.logMessage("Fail. (RemoveUnconfirmedTransactions)");		 
		 }	  
     }
   };

     public static Runnable GetUnconfirmedTransactions = new Runnable() { 
     public void run() {
     	
       try {     	
				Peer peer = Peer.GetRandomPeer( Peers.PEER_STATE_CONNECTED );
				if (peer != null) {					
				   JSONObject request = new JSONObject();
			      request.put("requestType", "GetUnconfirmedTransactions");						
			      request.put("serverURL", "http://"+peer.PeerAnnouncedAddress+"/api");
			      JSONObject response = peer.SendJsonQueryToPeer(request);				
				
				   JSONArray UnconfirmedTransactions = (JSONArray)response.get("UnconfirmedTransactions");
				   if (UnconfirmedTransactions.size() > 0) {
				       processTransactions( UnconfirmedTransactions, true );
				   }							
				}

		 } catch (Exception e) { 
           Helper.logMessage("Fail. (GetUnconfirmedTransactions)");		 
		 }	  
     }
   };
      
   
     public static void processTransactions(JSONArray TransactionsData, boolean UnconfirmedTransaction ) {

           JSONArray ValidatedTransactions = new JSONArray();
           boolean TransactionDoubleSpending ;
                      
           for (int i = 0; i < TransactionsData.size(); i++) {
           	   try {		
	               Transaction transaction = Transaction.GetTransactionFromJSON((JSONObject)TransactionsData.get(i));
	               String TransactionId = transaction.GetTransactionId();
	               // Helper.logMessage("Transaction ("+ i +") Id: "+TransactionId);
	               int curTime = Helper.getEpochTime(System.currentTimeMillis());
	               
	               if ( ( transaction.fee > 0 ) && ( transaction.deadline > 1 ) && ( transaction.timestamp < (curTime + 15) ) &&
	               ( transaction.timestamp + transaction.deadline > curTime ) ) {
	                     //Helper.logMessage("Transaction accepted."); 
								if (transactions.get(TransactionId) == null && UnconfirmedTransactions.get(TransactionId) == null 
								  && DoubleSpendingTransactions.get(TransactionId) == null && transaction.verify()) {
								     Helper.logMessage("Verify OK.");
								     
                             TransactionDoubleSpending = true;
                             String AccountAddress = Helper.PublicKeyToAddress(Helper.Base58decode(transaction.senderPublicKey)); 						
						           Account account = Accounts.getInstance().GetAccount(AccountAddress);
                             if (account != null) {
							            Helper.logMessage("Sender account exist. Unconfirmed balance: "+account.GetUnconfirmedBalance());
							            long amount = transaction.amount + transaction.fee;
							            synchronized (account) {								
								            if (account.GetUnconfirmedBalance() >= amount ) {																		
									               TransactionDoubleSpending = false;									
									               account.SetUnconfirmedBalance(account.GetUnconfirmedBalance() - amount );									
								            }								
							            }							
						           }								  	
                             // transaction.TransactionIndex = ++TransactionCounter;
									  if (TransactionDoubleSpending) {											
											DoubleSpendingTransactions.put(transaction.GetTransactionId(), transaction);											
									  } else {											
											UnconfirmedTransactions.put(transaction.GetTransactionId(), transaction);											
											if (!UnconfirmedTransaction) {												
												ValidatedTransactions.add(transaction);								  	
											}	  	
								  	  }								  									  	
		                  }	else {
		                     //Helper.logMessage("Transaction exist or verify fail.");
		                  }                   
	               }
	            } catch (Exception e) { 
	                Helper.logMessage("Bad transaction. "+e.toString());
	            }   
		      } // for transactions		
				
				if (ValidatedTransactions.size() > 0) {
				
				JSONObject peerRequest = new JSONObject();
				peerRequest.put("requestType", "processTransactions");
				peerRequest.put("ValidatedTransactions", ValidatedTransactions.toString());
				
				Peers peers = new Peers();				
				peers.SendToAllPeers(peerRequest);
				
			}

	   }

     private Transactions() {} // never

}




			
			
			
		

