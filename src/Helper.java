package bac.helper;

import bac.settings.Settings;
import bac.crypto.Crypto;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;


public final class Helper {

   static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final char[] ALPHABET_B58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
			.toCharArray();
	private static final int BASE_58 = ALPHABET_B58.length;
	private static final int BASE_256 = 256;

	private static final int[] INDEXES = new int[128];
	static {
		for (int i = 0; i < INDEXES.length; i++) {
			INDEXES[i] = -1;
		}
		for (int i = 0; i < ALPHABET_B58.length; i++) {
			INDEXES[ALPHABET_B58[i]] = i;
		}
	}
 

   public static void logMessage(String message) {
		 System.out.println((new StringBuilder((new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ")).format(new Date()))).append(message).toString());
		
	 }

   public static String PublicKeyToAddress(byte[] PublicKey) throws NoSuchAlgorithmException, UnsupportedEncodingException {
					
		StringBuilder AddrString = new StringBuilder();                         
      byte[] buffer;     	
				     			
		AddrString.append("B");
		AddrString.append(Base58encode((byte[]) PublicKey));							      		
		
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.reset();
      buffer = Base58decode(AddrString.toString());            
      md.update(buffer);
      byte[] digest = md.digest();
      	
		return Base58encode((byte[]) buffer)+Base58encode((byte[]) digest).substring(0,3);
		
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
	

   public static int getEpochTime(long time) {
		
		return (int)((time - Settings.epochBeginning + 500) / 1000);
		
	}
	


	public static String Base58encode(byte[] input) {
		if (input.length == 0) {
			// paying with the same coin
			return "";
		}

		//
		// Make a copy of the input since we are going to modify it.
		//
		input = copyOfRange(input, 0, input.length);

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input.length && input[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The actual encoding
		//
		byte[] temp = new byte[input.length * 2];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input.length) {
			byte mod = divmod58(input, startAt);
			if (input[startAt] == 0) {
				++startAt;
			}

			temp[--j] = (byte) ALPHABET_B58[mod];
		}

		//
		// Strip extra '1' if any
		//
		while (j < temp.length && temp[j] == ALPHABET_B58[0]) {
			++j;
		}

		//
		// Add as many leading '1' as there were leading zeros.
		//
		while (--zeroCount >= 0) {
			temp[--j] = (byte) ALPHABET_B58[0];
		}

		byte[] output = copyOfRange(temp, j, temp.length);
		return new String(output);
	}

	public static byte[] Base58decode(String input) {
		if (input.length() == 0) {
			// paying with the same coin
			return new byte[0];
		}

		byte[] input58 = new byte[input.length()];
		//
		// Transform the String to a base58 byte sequence
		//
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = INDEXES[c];
			}
			if (digit58 < 0) {
				throw new RuntimeException("Not a Base58 input: " + input);
			}

			input58[i] = (byte) digit58;
		}

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The encoding
		//
		byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			byte mod = divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}

		//
		// Do no add extra leading zeroes, move j to first non null byte.
		//
		while (j < temp.length && temp[j] == 0) {
			++j;
		}

		return copyOfRange(temp, j - zeroCount, temp.length);
	}

	private static byte divmod58(byte[] number, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			int digit256 = (int) number[i] & 0xFF;
			int temp = remainder * BASE_256 + digit256;

			number[i] = (byte) (temp / BASE_58);

			remainder = temp % BASE_58;
		}

		return (byte) remainder;
	}

	private static byte divmod256(byte[] number58, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			int digit58 = (int) number58[i] & 0xFF;
			int temp = remainder * BASE_58 + digit58;

			number58[i] = (byte) (temp / BASE_256);

			remainder = temp % BASE_256;
		}

		return (byte) remainder;
	}

	private static byte[] copyOfRange(byte[] source, int from, int to) {
		byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}
}
