package bac.test;

import bac.crypto.Crypto;
import bac.peers.Peers;
import bac.helper.Helper;
import bac.settings.Settings;
import bac.api.Api;
import bac.cron.Cron;
import bac.database.Database;
import bac.account.Accounts;
import bac.account.Account;
import bac.transaction.Transactions;
import bac.transaction.Transaction;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public final class Test  {


		private static Test instance = null;
      
		   
	   public static synchronized Test getInstance() {
	      if(instance == null) {
	         instance = new Test();
	      }
	      return instance;
	   }
	   
	   public static void StartTests() {
         try {
            Helper.logMessage("EpochBeginning:"+Settings.epochBeginning);
            Helper.logMessage("Testing Crypto module and helper functions.");

            byte[] PublicKey = new byte[32];                    
            PublicKey = Crypto.getPublicKey("secretPhrase");
            Helper.logMessage("PublicKey:"+Helper.Base58encode((byte[]) PublicKey));
            Helper.logMessage("BAC Address:"+Helper.PublicKeyToAddress((byte[]) PublicKey));
            
            byte[] signature = new byte[64];
            byte[] message = Helper.convert((String)"0123456789ABCDEF");
            Helper.logMessage("Message:"+Helper.convert((byte[]) message));
            signature = Crypto.sign(message,"secretPhrase");
            Helper.logMessage("Verify result:"+Boolean.toString(
            Crypto.verify(signature, message, PublicKey)));

            // Account Put/Get test (Sender account)   
            Account account = new Account("BrcLFiUk8SZFdjDNvFAg3NZZmqx2Fdy");
            account.SetUnconfirmedBalance(20000);
            Accounts.getInstance().PutAccount(account);
            account = Accounts.getInstance().GetAccount("BrcLFiUk8SZFdjDNvFAg3NZZmqx2Fdy");
            
            // Transaction test
            Thread.sleep(5550);
            
            

            Helper.logMessage("Recipient address: "+Helper.PublicKeyToAddress(Crypto.getPublicKey("RecipientSecretPhrase")));
            // B3yFMMk6zsw7ZsEhzbA9nwSb7cxZw2fu

            Transaction transaction = new Transaction(
                  Transaction.TYPE_ORDINARY_PAYMENT, ( 5 * 60 ), 
                  "7z1pmi6XifvGMhV7T1AxJsP8UsSVE5mP3SHKuDqd83xw", 
                  "B3yFMMk6zsw7ZsEhzbA9nwSb7cxZw2fu", 1050, 10, null);
				transaction.sign("secretPhrase");
				Helper.logMessage("Transaction verify "+transaction.verify());
															
				JSONObject peerRequest = new JSONObject();
				peerRequest.put("requestType", "ProcessTransactions");
				JSONArray transactionsData = new JSONArray();
				transactionsData.add(transaction.GetTransaction());
				peerRequest.put("ValidatedTransactions", transactionsData);
				
				Peers peers = new Peers();
				peers.SendToAllPeers(peerRequest);
	     } catch (Exception e) {
		     Helper.logMessage("Test fail. "+e.toString());
		  } 
	   }		   

}