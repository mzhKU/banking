package bank.local;

import bank.InactiveException;
import bank.OverdrawException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.text.DefaultEditorKit;

import static org.junit.Assert.*;

public class DriverTest {


    private Driver.Account account;
    private Driver.Bank bank;

    @BeforeEach
    void setup() {
        this.account = new Driver.Account("1");
        this.bank = new Driver.Bank();
    }

    @Test
    void testInit() {
        System.out.println("here");
    }

    @Test
    void testGetInexistingAccount() {
        Driver.Account accountOne = new Driver.Account("Thomas");
        this.bank.createAccount("Thomas");
        // When
        try {
            // this.bank.accounts.put("1", accountOne);
        } catch (Exception e) {
            System.out.println(e);
        }

        Assertions.assertTrue(this.bank.closeAccount("1"));
        Assertions.assertFalse(this.bank.closeAccount("2"));
    }

    @Test
    void testDepositNegativeAmount() {

        Driver.Account accountOne = new Driver.Account("Thomas");
        this.bank.createAccount("Thomas");

        try {
            double x = Math.floor(Math.sin(100) * 100) / 10;
            accountOne.deposit(x);
        } catch (IllegalArgumentException e) {
            System.out.println("Deposit should be positive");
        } catch (InactiveException e) {
            System.out.println("Account must be active");
        }
    }

    @Test
    void testOverdraw() {
        Driver.Account accountOne = new Driver.Account("Thomas");
        this.bank.createAccount("Thomas");

        try {
            double x = Math.floor(Math.sin(100) * 100) / 10;
            accountOne.deposit(x);
        } catch (InactiveException e) {
            System.out.println("Account inactive");
        }

        Assertions.assertTrue(accountOne.getBalance() > 0);
    }

    @Test
    void testAddAccountWithSameAccountNumberTwice() {
        Driver.Account accountOne = new Driver.Account("Thomas");
        Driver.Account accountTwo = new Driver.Account("Michael");

        // When
        try {
            // this.bank.accounts.put("1", accountOne);
            // this.bank.accounts.put("1", accountTwo);
        } catch (Exception e) {
            System.out.println(e);
        }

        // System.out.println(this.bank.accounts.keySet());
        try {
            System.out.println(this.bank.getAccount("1").getOwner());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void testCloseAccount() {
        // given
        Driver.Bank newBank = new Driver.Bank();
        Driver.Account accountToClose = new Driver.Account("10");

        // when
        newBank.createAccount("Thomas");
    }

    @Test
    void testDepositAndGetBalance() throws InactiveException {
        // given
        Double amountToDeposit = 10.0;

        // when
        try {
            this.account.deposit(10);
        } catch (InactiveException e) {
            throw e;
        }

        // then
        Assertions.assertEquals(this.account.getBalance(), amountToDeposit);
    }

}