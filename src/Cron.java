package bac.cron;

import bac.helper.Helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Cron {
	
   static ScheduledExecutorService CronThreads = Executors.newScheduledThreadPool(2);

    public static void stop() {
         CronThreads.shutdown();
        	Helper.logMessage("Cron services stoped.");        
    }

   public static synchronized void AddCronThread(Runnable runnable, Integer delay) {
   	  CronThreads.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        Helper.logMessage("Cron service ("+runnable.toString()+") started.");
                
    }   
      
    private Cron() {} //never

}
