package bac.blockchain;

import bac.helper.Helper;

import java.io.Serializable;
import org.json.simple.JSONArray;

public final class Block implements Serializable {
		
		int Version;
		int Timestamp;
		long BlockHeight;
		String PreviousBlockID;
		String NextBlockID;
		String GeneratorAddress;
		String BlockSignature;
		int numberOfTransactions;
		long totalAmount; 
		long totalFee;
      JSONArray Transactions;

      String GetBlockID() {
      	try {
          return Helper.Base58encode(((String)"LastBlockID-LastBlockID-LastBlockID").getBytes("UTF-8"));
          } catch (Exception e) {
          return null;		  
		  }      
      }
}

