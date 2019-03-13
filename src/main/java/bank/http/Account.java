package bank.http;

import bank.InactiveException;
import bank.OverdrawException;

public class Account implements bank.Account {
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

    public void setActive(boolean active) {
        this.active = active;
    }
}
