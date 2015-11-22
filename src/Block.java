package bac.blockchain;

import bac.helper.Helper;
import bac.crypto.Crypto;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.security.MessageDigest;

public final class Block implements Serializable {
						
      public static final int BLOCKTYPE_ORPHAN = 0;	
      public static final int BLOCKTYPE_NORMAL = 1;	
              
		int Version;
		int Timestamp;
		int BlockType;
		long BlockHeight;
		
		String PreviousBlockID;
		
		String HeaderSignature;
             
		String BlockSignature;
		String GeneratorSignature;   // ForgeBlockID+TransactionsHash 
		String GeneratorPublicKey;
		String ForgeBlockID;

		
		int numberOfTransactions;
		long totalAmount; 
		long totalFee;
		
		String TransactionsHash;
      JSONArray Transactions;
      JSONArray Certifications;  // array of ForgeBlockID+TransactionsHash

      public JSONObject GetBlockHeader() {
			
			JSONObject blockheader = new JSONObject();
			
			blockheader.put("Version", Version);
			blockheader.put("Timestamp", Timestamp);
			blockheader.put("BlockHeight", BlockHeight);
			blockheader.put("PreviousBlockID", PreviousBlockID);
						
			return blockheader;
			
		}

      
      public JSONObject GetBlock() {
			
			JSONObject block = new JSONObject();
			
			block.put("Version", Version);
			block.put("Timestamp", Timestamp);
			block.put("BlockHeight", BlockHeight);
			block.put("PreviousBlockID", PreviousBlockID);
						
			return block;
			
		}

      public JSONObject GetBlockData() {
			
			JSONObject block = new JSONObject();
			
			block.put("Version", Version);
			block.put("Timestamp", Timestamp);
			block.put("BlockType", BlockType);
			block.put("BlockHeight", BlockHeight);
			block.put("PreviousBlockID", PreviousBlockID);
						
			return block;
			
		}


     void SignBlockHeader(String secretPhrase) {
      	try {
             HeaderSignature = Helper.Base58encode(Crypto.sign(GetBlockHeader().toString().getBytes("UTF-8"),secretPhrase));
          } catch (Exception e) {
             HeaderSignature = null;		  
		    }       
     }




      String GetBlockID() {
      	try {
             return Helper.Base58encode(MessageDigest.getInstance("SHA-256").digest(GetBlock().toString().getBytes("UTF-8")));
          } catch (Exception e) {
             return null;		  
		  }      
      }
      
      void SetBlockTimestamp(int Timestamp) { 
         this.Timestamp = Timestamp;      
      }      
      
      int GetBlockTimestamp() { 
         return Timestamp;      
      }

      void SetBlockVersion(int Version) { 
         this.Version = Version;      
      }      
      
      int GetBlockVersion() { 
         return Version;      
      }

      void SetBlockBlockHeight(long BlockHeight) { 
         this.BlockHeight = BlockHeight;      
      }      
      
      long GetBlockBlockHeight() { 
         return BlockHeight;      
      }

      void SetBlockPreviousBlockID(String PreviousBlockID) { 
         this.PreviousBlockID = PreviousBlockID;      
      }      
      
      String GetBlockPreviousBlockID() { 
         return PreviousBlockID;      
      }


      void SetBlockHeaderSignature(String HeaderSignature) { 
         this.HeaderSignature = HeaderSignature;      
      }      

      String GetBlockHeaderSignature() { 
         return HeaderSignature;      
      }

      void SetBlockType(int BlockType) { 
         this.BlockType = BlockType;      
      }      
      
      int GetBlockType() { 
         return BlockType;      
      }

      
      void GenerateBlock() {
               
      }
}

