package bac.helper;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class Helper {

   static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
   static final BigInteger two64 = new BigInteger("12345678901234567890");

   public static void logMessage(String message) {
		 System.out.println((new StringBuilder((new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ")).format(new Date()))).append(message).toString());
		
	 }
                                                   
	public static byte[] convert(String string) {
				
		byte[] bytes = new byte[string.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			
			bytes[i] = (byte)Integer.parseInt(string.substring(i * 2, i * 2 + 2), 16);
			
		}
		
		return bytes;
		
	}
	
	public static String convert(byte[] bytes) {
		
		StringBuilder string = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			
			int number;
			string.append(alphabet.charAt((number = bytes[i] & 0xFF) >> 4)).append(alphabet.charAt(number & 0xF));
			
		}
		
		return string.toString();
		
	}
	
	public static String convert(long objectId) {
		
		BigInteger id = BigInteger.valueOf(objectId);
		if (objectId < 0) {
			
			id = id.add(two64);
			
		}
		
		return id.toString();
		
	}

}
