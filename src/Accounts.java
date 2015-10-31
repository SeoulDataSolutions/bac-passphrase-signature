package bac.account;

import bac.account.Account;

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
   
   public static void PutAccount(Account account) {
     accounts.put(account.Address,account);   
   }
               
   protected Accounts() {} 
}