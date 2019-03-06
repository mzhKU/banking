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

                            out.writeUTF(bank.getAccount(accountNumber).getNumber());
                            out.flush();
                            out.writeUTF(bank.getAccount(accountNumber).getOwner());
                            out.flush();

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
                                throw new IllegalArgumentException();
                            }
                            if(from.getBalance() < amount) {
                                throw new OverdrawException();
                            } else {
                                try {
                                    bank.transfer(from, to, amount);
                                    out.writeUTF("ok");
                                    out.flush();
                                } catch (InactiveException e) {
                                    System.out.println("[Server:transfer]Inactive exception");
                                    // this.out.writeBoolean(false);
                                    out.writeUTF("inactive");
                                    out.flush();
                                } catch (OverdrawException o) {
                                    System.out.println("[Server:transfer]Overdraw exception");
                                    out.writeUTF("overdraw");
                                    out.flush();
                                } catch (IllegalArgumentException i) {
                                    System.out.println("[Server:transfer]Illegal argument");
                                    System.out.println("[Server:transfer]from.balance: " + from.getBalance());
                                    System.out.println("[Server:transfer]amount: " + amount);
                                    out.writeUTF("illegal");
                                    out.flush();
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
                            try {
                                bank.getAccount(in.readUTF()).deposit(in.readDouble());
                            } catch (InactiveException e) {
                                System.out.println("Account inactive");
                                // this.out.writeBoolean(false);
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
                            System.out.println("[Server]Close account.");
                            String closeAccount = this.in.readUTF();
                            if (bank.getAccount(closeAccount).isActive()) {
                                bank.closeAccount(closeAccount);
                                out.writeBoolean(true);
                            } else {
                                out.writeBoolean(false);
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
