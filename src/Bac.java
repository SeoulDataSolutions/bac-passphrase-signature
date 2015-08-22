package bac;

import bac.crypto.Crypto;
import bac.helper.Helper;
import bac.settings.Settings;


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

    public static void init() {
        Init.init();
    }
	
    public static void shutdown() {        
        Helper.logMessage("Bac server " + Settings.VERSION + " stopped.");        
    }

    private static class Init {

        static {

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
            byte[] signature = new byte[64];
            byte[] message = Helper.convert((String)"0123456789ABCDEF");
            Helper.logMessage("Message:"+Helper.convert((byte[]) message));
            signature = Crypto.sign(message,"secretPhrase");
            Helper.logMessage("Verify result:"+Boolean.toString(
            Crypto.verify(signature, message, PublicKey)));

            
               
            
        }

        private static void init() {}

        private Init() {} // never

    }

    private Bac() {} // never
	
}
