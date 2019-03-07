package bank.sockets;

import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {

    private static bank.Bank bank = new bank.local.Driver.Bank();
    // private static bank.Bank bank = new bank.sockets.Driver().getBank();

    // ClassNotFoundException: if readObject() doesn't return the object type expected
    public static void main(String args[]) throws IOException {

        // Port is defined server side by the service provider
        // It has to be known by the client, to which server we're
        // connecting. The port is *not* provided from the client
        // to the server.
        int serverPort = 1234;

        ServerSocket server = new ServerSocket(serverPort);
        System.out.println("Startet Bank Server on port " + server.getLocalPort());
        while(true) {
            Socket s = server.accept();
            Thread t = new Thread(new BankHandler(bank, s));
            t.start();
        }
    }

    private static class BankHandler implements Runnable {

        private DataOutputStream out;
        private DataInputStream in;
        private bank.Bank bank;
        private Socket socket;

        public BankHandler(bank.Bank bank, Socket socket) {
            this.socket = socket;
            this.bank = bank;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    out = new DataOutputStream(socket.getOutputStream());
                    in  = new DataInputStream(  socket.getInputStream());
                    String arg = in.readUTF();
                    System.out.println("[Server:try]arg: " + arg);
                    switch (arg) {
                        // ------------------------------------------------------------------
                        case "createAccount":
                            String owner = in.readUTF();
                            System.out.println("[Server:createAccount]Owner: " + owner);

                            String id = bank.createAccount(owner);
                            System.out.println("[Server:createAccount]Id: " + id);

                            out.writeUTF("" + id);
                            out.flush();
                            // out.writeUTF(owner);
                            // out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "getAccount":
                            String accountNumber = in.readUTF();
                            System.out.println("[Server:getAccount]Account number: " + accountNumber);
                            System.out.println("[Server:getAccount]Bank account: " + bank.getAccount(accountNumber));

                            if(bank.getAccount(accountNumber) == null) {
                                out.writeUTF("");
                                out.writeUTF("");
                                out.flush();
                            } else {
                                out.writeUTF(bank.getAccount(accountNumber).getNumber());
                                out.flush();
                                out.writeUTF(bank.getAccount(accountNumber).getOwner());
                                out.flush();
                            }

                            break;
                        // ------------------------------------------------------------------
                        case "transfer":
                            Account from = bank.getAccount(in.readUTF());
                            Account to = bank.getAccount(in.readUTF());
                            Double amount = in.readDouble();

                            System.out.println("[Server:transfer]From: " + from);
                            System.out.println("[Server:transfer]to: " + to);

                            if(!from.isActive() || !to.isActive()) {
                                out.writeUTF("inactive");
                                out.flush();
                                break;
                            }
                            if(amount < 0.0) {
                                out.writeUTF("illegal");
                                out.flush();
                                break;
                            }
                            if(from.getBalance() < amount) {
                                out.writeUTF("overdraw");
                                out.flush();
                                break;
                            } else {
                                try {
                                    bank.transfer(from, to, amount);
                                } catch (InactiveException e) {
                                    System.out.println("[Server:transfer]Inactive exception");
                                    // this.out.writeBoolean(false);
                                } catch (OverdrawException o) {
                                    System.out.println("[Server:transfer]Overdraw exception");
                                } catch (IllegalArgumentException i) {
                                    System.out.println("[Server:transfer]Illegal argument");
                                    System.out.println("[Server:transfer]from.balance: " + from.getBalance());
                                    System.out.println("[Server:transfer]amount: " + amount);
                                }
                            }
                            break;
                        // ------------------------------------------------------------------
                        case "active" :
                            out.writeBoolean(bank.getAccount(in.readUTF()).isActive());
                            out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "getBalance" :
                            String getBalanceFrom = new DataInputStream(socket.getInputStream()).readUTF();
                            System.out.println("[Server:getBalance]getBalanceFrom: " + getBalanceFrom);
                            System.out.println("[Server:getBalance]Balance: " + bank.getAccount(getBalanceFrom).getBalance());
                            out.writeDouble(bank.getAccount(getBalanceFrom).getBalance());
                            out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "deposit" :
                            String accountToDeposit = in.readUTF();
                            double amountToDeposit = in.readDouble();
                            try {
                                bank.getAccount(accountToDeposit).deposit(amountToDeposit);
                                out.writeUTF("ok");
                            } catch (InactiveException e) {
                                System.out.println("Account inactive");
                                out.writeUTF("inactive");
                                // this.out.writeBoolean(false);
                            } catch (IllegalArgumentException e) {
                                out.writeUTF("illegal");
                                out.flush();
                            }
                            break;
                        case "withdraw" :
                            try {
                                bank.getAccount(in.readUTF()).withdraw(in.readDouble());
                            } catch (InactiveException e) {
                                System.out.println("Account inactive");
                                // this.out.writeBoolean(false);
                            }
                            break;
                        // ------------------------------------------------------------------
                        case "getAccountNumbers":
                            System.out.println("[Server]Get account numbers.");
                            ObjectOutputStream accountNumbersOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            accountNumbersOutputStream.writeObject(bank.getAccountNumbers());
                            break;
                        // ------------------------------------------------------------------
                        case "closeAccount":
                            String closeAccount = in.readUTF();

                            if(!bank.getAccountNumbers().contains(closeAccount)) {
                                out.writeUTF("non-exist");
                                break;
                            }

                            if(bank.getAccount(closeAccount).getBalance() > 0.0) {
                                out.writeUTF("nonzero");
                                break;
                            }

                            if (!bank.getAccount(closeAccount).isActive()) {
                                out.writeUTF("inactive");
                                break;
                            }

                            out.writeUTF("ok");
                            try {
                                bank.closeAccount(closeAccount);
                            } catch (InactiveException e) {
                                System.out.println("[Server:close]Inactive exception");
                            } catch (IllegalArgumentException i) {
                                System.out.println("[Server:close]Illegal argument");
                                System.out.println("[Server:close]from.balance: " + bank.getAccount(closeAccount).getBalance());
                            }

                            break;
                        // ------------------------------------------------------------------
                        default:
                            System.out.println("No instruction");
                            break;
                        // ------------------------------------------------------------------
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (OverdrawException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
