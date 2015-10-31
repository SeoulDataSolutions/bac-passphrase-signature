package bac.account;

public final class Account {

      static String Address;
      static String PublicKey;
		static long Balance;
		static long UnconfirmedBalance;
		
		public  Account(String Address) {			
			this.Address = Address;
		} 
		
		public static long GetUnconfirmedBalance() {			
			return UnconfirmedBalance;
		}	
		
		public void SetUnconfirmedBalance(long unconfirmedBalance) {			
			this.UnconfirmedBalance = unconfirmedBalance;
		}	
		
		public static long GetBalance() {			
			return Balance;
		}	
		
		public void SetBalance(long Balance) {			
			this.Balance = Balance;
		}

		public static String GetPublicKey() {			
			return PublicKey;
		}	
		
		public void SetPublicKey(String PublicKey) {			
			this.PublicKey = PublicKey;
		}

}
