package bac;

import bac.crypto.Crypto;
import bac.peers.Peers;
import bac.helper.Helper;
import bac.settings.Settings;
import bac.api.Api;
import bac.cron.Cron;
import bac.database.Database;

public final class Bac  {
  
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Bac.shutdown();
            }
        }));
        init();
    }

    public static void init()  {
    	
		try {
           long startTime = System.currentTimeMillis();

            Helper.logMessage("logging enabled");
					
            long currentTime = System.currentTimeMillis();
            Settings.init();
            Helper.logMessage("Initialization took " + (currentTime - startTime) / 1000 + " seconds");
            Helper.logMessage("Bac server " + Settings.VERSION + " started successfully.");
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
            Database.init();
            Api.init();
            Peers.init();            
		} catch (Exception e) {
		     Helper.logMessage("Error starting BAC server.");
		}    	
    }
	
    public static void shutdown() {
    	  Cron.stop();        
    	  Database.stop();
        Helper.logMessage("Bac server " + Settings.VERSION + " stopped.");        
    }

  
    private Bac() {} // never
	
}
