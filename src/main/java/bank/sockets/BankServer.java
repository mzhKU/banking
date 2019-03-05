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
                    this.in = new DataInputStream(socket.getInputStream());
                    this.out = new DataOutputStream(socket.getOutputStream());
                    String arg = in.readUTF();
                    switch (arg) {
                        // ------------------------------------------------------------------
                        case "createAccount":
                            String owner = in.readUTF();
                            System.out.println("[Server]createAccount: " + owner);

                            String id = bank.createAccount(owner);
                            System.out.println("[Server]id: " + id);

                            this.out.writeUTF("" + id);
                            this.out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "transfer":
                            DataInputStream transferStream = new DataInputStream(in);

                            Account from = bank.getAccount(transferStream.readUTF());
                            Account to = bank.getAccount(transferStream.readUTF());
                            Double amount = in.readDouble();

                            if(!from.isActive() || !to.isActive()) {
                                out.writeUTF("inactive");
                                out.flush();
                            }

                            try {
                                bank.transfer(from, to, amount);
                                out.writeUTF("ok");
                            } catch (InactiveException e) {
                                System.out.println("[Server]Inactive exception raised");
                                // this.out.writeBoolean(false);
                                out.writeUTF("inactive");
                                out.flush();
                            } catch (OverdrawException o) {
                                out.writeUTF("overdraw");
                                out.flush();
                            } catch (IllegalArgumentException i) {
                                out.writeUTF("illegal");
                                out.flush();
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
                            out.writeDouble(bank.getAccount(getBalanceFrom).getBalance());
                            out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "deposit" :
                            double amountToDeposit = new DataInputStream(socket.getInputStream()).readDouble();
                            String a = new DataInputStream(socket.getInputStream()).readUTF();
                            try {
                                bank.getAccount(a).deposit(amountToDeposit);
                            } catch (InactiveException e) {
                                System.out.println("Account inactive");
                                this.out.writeBoolean(false);
                            }
                        case "withdraw" :
                            double toWithdraw = new DataInputStream(socket.getInputStream()).readDouble();
                            String b = new DataInputStream(socket.getInputStream()).readUTF();
                            try {
                                bank.getAccount(b).withdraw(toWithdraw);
                            } catch (InactiveException e) {
                                System.out.println("Account inactive");
                                this.out.writeBoolean(false);
                            }
                        // ------------------------------------------------------------------
                        case "getAccount":
                            System.out.println("[Server]Get account.");
                            // ObjectOutputStream accountOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            //accountOutputStream.writeObject(bank.getAccount(in.readUTF()));
                            DataOutputStream accountStream = new DataOutputStream(socket.getOutputStream());
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String accountToGet = in.readUTF();
                            System.out.println("[Server]Account number: " + accountToGet);
                            System.out.println("[Server]Bank account: " + bank.getAccount(accountToGet));

                            accountStream.writeUTF(bank.getAccount(accountToGet).getOwner());
                            accountStream.flush();

                            accountStream.writeUTF(bank.getAccount(accountToGet).getNumber());
                            accountStream.flush();
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
