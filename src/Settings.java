package bac.settings;

import bac.helper.Helper;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Properties;
import java.util.Properties;
import bac.crypto.Crypto;

import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public final class Settings {
	
   public static final String GenesisHeaderSignature ="4aU6qRfjnAp1dPzkjod2sprsSG8HZSv6UZJSEA8BQHkyoA88BhdJvMGgdMgpXQv77JMTgsD5eqc2rHdfvL6A3QhU";

   public static final String VERSION = "BAC V1.0";
   public static final String CONFIG_FILE = "settings.conf";
   private static final Properties defaultProperties = new Properties();

   public static int APIport = 8080;
   public static String APIhost = "127.0.0.1";
   public static String SeedNodes = "127.0.0.1:8080;127.0.0.1:8081";
   public final static int APItimeout = 15000;   

   public static String NodeSecretPhrase ="SecretPhrase";
   public static String NodePublicKey ="";
   public static String NodeBountyAddress ="";

   public static long epochBeginning;

   public final static int BlockTime = 30;

   public static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";


   public static void init() {
        Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.ZONE_OFFSET, 0);
			calendar.set(Calendar.YEAR, 2015);
			calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
			calendar.set(Calendar.DAY_OF_MONTH, 14);
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			epochBeginning = calendar.getTimeInMillis();
			LoadSettings(CONFIG_FILE);
    }

    private static void LoadSettings(String propertiesFile) {
    	   try {
    	     InputStream is = new FileInputStream(propertiesFile);
		         try {         			         	        
			         if (is != null) {
			             Helper.logMessage("Loading "+propertiesFile+" config file.\n");         
			             defaultProperties.load(is);
			             if (defaultProperties.getProperty("APIhost") != null)
			               APIhost = defaultProperties.getProperty("APIhost");
			             if (defaultProperties.getProperty("APIport") != null)
			               APIport = Integer.parseInt(defaultProperties.getProperty("APIport"));
			             if (defaultProperties.getProperty("SeedNodes") != null)
			               SeedNodes = defaultProperties.getProperty("SeedNodes");
			             if (defaultProperties.getProperty("NodeSecretPhrase") != null) {
			                NodeSecretPhrase = defaultProperties.getProperty("NodeSecretPhrase");
			                try {
			                   NodePublicKey = Helper.Base58encode((byte[]) Crypto.getPublicKey(NodeSecretPhrase));
                            NodeBountyAddress = Helper.PublicKeyToAddress(Helper.Base58decode(NodePublicKey));
                            Helper.logMessage("Node Bounty Address:"+NodeBountyAddress);
                         } catch (Exception e) {
		                      Helper.logMessage("Bounty address generation fail. "+e.toString());
		                   } 

			             }
			               
			         } 
		         } catch (IOException e) {
		                    Helper.logMessage("Config file read IOException.\n");
		                }
    	   } catch (IOException e2) {
		         try {         	
                  Files.createFile(Paths.get(propertiesFile));
                  Files.write(Paths.get(propertiesFile), "# use this file for different settings".getBytes());
                  Helper.logMessage("Created empty config file ("+propertiesFile+")\n");
               } catch (IOException e) {
		                    Helper.logMessage("Config file create IOException.\n");
		         }
    	   }
    
   }
    
   private Settings() {} // never

}
