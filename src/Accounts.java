package bac.account;

import bac.account.Account;
import bac.helper.Helper;

import java.util.HashMap;

public final class Accounts {

   private static Accounts instance = null;
   private static HashMap<String, Account> accounts = new HashMap<>();      
   
   public static Accounts getInstance() {
      if(instance == null) {
         instance = new Accounts();
      }
      return instance;
   }
   
   public static Account GetAccount(String AccountAddress) {
     return accounts.get(AccountAddress);   
   }
   

   public static Account GetPubkeyAccount(String PublicKey) {
     try {
       return accounts.get(Helper.PublicKeyToAddress( Helper.Base58decode(PublicKey) ));
     } catch (Exception e) {
     	 return null;  
     }	
        
   }
   
   
   
   
   public static void PutAccount(Account account) {
     accounts.put(account.Address,account);   
   }
               
   protected Accounts() {} 
}