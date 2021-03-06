/*
 * Copyright (c) 2019 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.local;

import bank.InactiveException;
import bank.OverdrawException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	public static class Bank implements bank.Bank {

	    // Kontonummer: String
		private final Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
		    Set<String> activeAccounts = new HashSet<>();
		    for(String accountNumber : accounts.keySet()) {
                if(accounts.get(accountNumber).isActive()) {
                    activeAccounts.add(accountNumber);
                }
            }
			return activeAccounts;
		}

		@Override
		public String createAccount(String owner) {
			System.out.println("[local.bank:createAccount]Owner: " + owner);
		    String accountNumber = Integer.toString(accounts.size() + 1);
		    this.accounts.put(accountNumber, new Account(owner, accountNumber));
			return accountNumber;
		}

		@Override
		public boolean closeAccount(String number) {
			try {
                if(this.getAccountNumbers().contains(number)) {
                    if(!this.getAccount(number).isActive()) {
                        return false;
                    }
					// Unduplicate
                    if(true) {}
                    if(this.getAccount(number).getBalance() == 0) {
                        accounts.get(number).active = false;
                        return true;
                    }
                }
            } catch (IOException e) {
			    System.out.println("IOException from 'closeAccount': " + e);
            }
			return false;
		}

		@Override
		public bank.Account getAccount(String number) {
            return accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount) throws IOException, InactiveException, OverdrawException {
		    if(!from.isActive() || !to.isActive()) {
		        throw new InactiveException("Inactive accounts involved in transfer.");
            } else if (amount < 0.0) {
                throw new IllegalArgumentException("Amount transfered must be positive");
            } else if(from.getBalance() < amount) {
                throw new OverdrawException("Insufficient funds");
            } else {
                from.withdraw(amount);
                to.deposit(amount);
            }
		}
	}

public static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active = true;

		public Account(String owner, String number) {
			this.owner = owner;
		    this.number = number;
		}

		@Override
		public double getBalance() {
			return balance;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void deposit(double amount) throws InactiveException, IllegalArgumentException {
		    if(!this.isActive()) {
		        throw new InactiveException("Account inactive");
            }
            if(amount < 0.0) {
                throw new IllegalArgumentException("Deposited amount should be positive");
            } else {
                this.balance += amount;
            }
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
		    if(!this.active) {
		        throw new InactiveException("Account inactive.");
            }
		    if(this.balance < amount) {
		        throw new OverdrawException("Balance to low to withdraw " + Double.toString(amount) + ".");
            }
		    this.balance -= amount;
		}
	}
}