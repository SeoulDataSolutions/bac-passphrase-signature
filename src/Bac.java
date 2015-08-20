package bac;

import bac.crypto.Crypto;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class Bac  {

public static final String VERSION = "BAC V1.0";

    static void logMessage(String message) {
		 System.out.println((new StringBuilder((new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ")).format(new Date()))).append(message).toString());
		
	 }

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
        Bac.logMessage("Bac server " + VERSION + " stopped.");        
    }

    private static class Init {

        static {

            long startTime = System.currentTimeMillis();

            Bac.logMessage("logging enabled");
					
            long currentTime = System.currentTimeMillis();
            Bac.logMessage("Initialization took " + (currentTime - startTime) / 1000 + " seconds");
            Bac.logMessage("Bac server " + VERSION + " started successfully.");
            
            Bac.logMessage("Testing Crypto module");

            byte[] PublicKey = new byte[32];                        
            PublicKey = Crypto.getPublicKey("secretPhrase");
            byte[] signature = new byte[64];
            byte[] message = { 0,1,2,3,4,5,6,7,8,9 };
            signature = Crypto.sign(message,"secretPhrase");
            Bac.logMessage("Verify result:"+Boolean.toString(
            Crypto.verify(signature, message, PublicKey)));
            
        }

        private static void init() {}

        private Init() {} // never

    }

    private Bac() {} // never
	
}
