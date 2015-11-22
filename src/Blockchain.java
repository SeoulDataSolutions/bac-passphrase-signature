package bac.blockchain;

import bac.settings.Settings;
import bac.helper.Helper;
import java.util.HashMap;

public final class Blockchain {

    public static int LastBlockTime = 0;
    static long LastBlock = 0;
    static int blockCounter;
	 static HashMap<Long, Block> blocks;


    private static Blockchain instance = null;     
   
    public static Blockchain getInstance() {
      if(instance == null) {
         instance = new Blockchain();
      }
      return instance;
    }  
		
		public static Block GetLastBlock() {
		  return GetGenesisBlock();         		
		}
		
		public static Block GetGenesisBlock() {
		  Block block = new Block();
        block.SetBlockTimestamp(Helper.getEpochTime(Settings.epochBeginning));
        block.SetBlockVersion(1);
        block.SetBlockBlockHeight(1);
        block.SetBlockPreviousBlockID(null);
        block.SetBlockType(block.BLOCKTYPE_NORMAL);
        // block.SignBlockHeader("GenesisSecretPhrase");
        // Helper.logMessage("GenesisHeaderSignature:"+block.GetBlockHeaderSignature());
        
        
		  return block;         		
		}
		

}

