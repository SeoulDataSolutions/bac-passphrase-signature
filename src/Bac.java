package bac;

import bac.crypto.Crypto;
import bac.peers.Peers;
import bac.helper.Helper;
import bac.test.Test;
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
				Helper.logMessage("Starting BAC server...");	
            Settings.init();            
            Database.init();
            Api.init();
            Peers.init();
            Transactions.getInstance().init();  
            Helper.logMessage("Bac server " + Settings.VERSION + " started successfully."); 
            Test.StartTests();
		} catch (Exception e) {
		     Helper.logMessage("Error ("+e.toString()+") starting BAC server.");
		}    	
    }
	
    public static void shutdown() {
    	  Cron.stop();        
    	  Database.stop();
        Helper.logMessage("Bac server " + Settings.VERSION + " stopped.");        
    }

  
    private Bac() {} // never
	
}
