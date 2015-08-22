package bac.settings;

import java.util.Calendar;
import java.util.TimeZone;

public final class Settings {

   public static final String VERSION = "BAC V1.0";

   public static long epochBeginning;

   public static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";


   public static void init() {
        Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.ZONE_OFFSET, 0);
			calendar.set(Calendar.YEAR, 2015);
			calendar.set(Calendar.MONTH, Calendar.AUGUST);
			calendar.set(Calendar.DAY_OF_MONTH, 20);
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			epochBeginning = calendar.getTimeInMillis();
    }
    
    private Settings() {} // never

}
