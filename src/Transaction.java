package bac.transaction;

import bac.helper.Helper;
import bac.crypto.Crypto;

import java.io.Serializable;
import java.security.MessageDigest;

import org.json.simple.JSONObject;

public final class Transaction implements Comparable<Transaction> {

		public static final int TYPE_ORDINARY_PAYMENT = 1;		
		public static final int TYPE_NODE_BOUNTY = 2;

      // Signed Transaction Data
      int version;
      int type;
		int timestamp;
		int deadline;
		String senderPublicKey;
		String recipient;
		long amount;
		long fee;		
		
		String signature;		
	   
      public Transaction( int type,	int deadline, String senderPublicKey, String recipient, long amount, long fee, String signature ) {
			
	      this.version=1;
	      this.type=type;
			this.timestamp=Helper.getEpochTimestamp();
			this.deadline=deadline;
			this.senderPublicKey=senderPublicKey;
			this.recipient=recipient;
			this.amount=amount;
			this.fee=fee;
			this.signature=signature;
		}	
		
		public int compareTo(Transaction o) {
			
         if (( o.fee != 0 ) && ( this.fee != 0)) {
             if ((this.amount/this.fee) > (o.amount/o.fee)) return 1;
			    if ((this.amount/this.fee) < (o.amount/o.fee)) return -1;         
         } 	
         
         if ( o.fee == 0 ) return 1;
         if ( this.fee == 0 ) return -1;

			if (this.timestamp < o.timestamp ) return -1;
			if (this.timestamp > o.timestamp ) return 1;
						
			return 0;
			
		}   
	   
	   public JSONObject GetTransaction() {
			
			JSONObject transaction = new JSONObject();
			
			transaction.put("version", version);
			transaction.put("type", type);
			transaction.put("timestamp", timestamp);
			transaction.put("deadline", deadline);
			transaction.put("senderPublicKey", senderPublicKey);
			transaction.put("recipient", recipient);
			transaction.put("amount", amount);
			transaction.put("fee", fee);			
			transaction.put("signature", signature);
						
			return transaction;
			
		}

      static Transaction GetTransactionFromJSON(JSONObject transactionData) {

          Transaction transaction = new Transaction(	           
	           ((Long)transactionData.get("type")).intValue(),
	           ((Long)transactionData.get("deadline")).intValue(),
	           (String)transactionData.get("senderPublicKey"),
	           (String)transactionData.get("recipient"),
	           ((Long)transactionData.get("amount")).longValue(), 
	           ((Long)transactionData.get("fee")).longValue(),
	           (String)transactionData.get("signature")
          );
          transaction.version = ((Long)transactionData.get("version")).intValue();
          transaction.timestamp = ((Long)transactionData.get("timestamp")).intValue();
          return transaction;          
		}

      JSONObject GetTransactionData() {
			
			JSONObject transaction = new JSONObject();
			
			transaction.put("version", version);
			transaction.put("type", type);
			transaction.put("timestamp", timestamp);
			transaction.put("deadline", deadline);
			transaction.put("senderPublicKey", senderPublicKey);
			transaction.put("recipient", recipient);
			transaction.put("amount", amount);
			transaction.put("fee", fee);						
			
			return transaction;
			
		}
		
      public String GetTransactionId() throws Exception {
			try {
			   return Helper.Base58encode(MessageDigest.getInstance("SHA-256").digest(GetTransaction().toString().getBytes("UTF-8")));
			} catch (Exception e) {
            return null;			
			}
		}
		
		public void sign(String secretPhrase) throws Exception {
			try{
				signature = Helper.Base58encode(Crypto.sign(GetTransactionData().toString().getBytes("UTF-8"),secretPhrase));
	         if (!verify()) {
	            signature = null;
	         }			
			} catch (Exception e) {
            signature = null;			
			}
						
		}
		
		public boolean verify() throws Exception {
		      try {	
			       return Crypto.verify(Helper.Base58decode(signature), 
			         GetTransactionData().toString().getBytes("UTF-8"), Helper.Base58decode(senderPublicKey));
				} catch (Exception e) {
            return false;			
			}

		}

     public int GetTimestamp() {
        return timestamp;     
     }	 
}

