package bank.http;

import bank.InactiveException;
import bank.OverdrawException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Bank implements bank.Bank {

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
                if(this.getAccount(number).getBalance() == 0) {
                    accounts.get(number).setActive(false);
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
