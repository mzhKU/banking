package bank.sockets;

import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {

    private static bank.Bank bank = new bank.local.Driver.Bank();

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
                    //this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    this.out = new DataOutputStream(socket.getOutputStream());
                    String arg = in.readUTF();
                    System.out.println("[Server]Command: " + arg);
                    // arg: "createOwner:<owner>"
                    switch(arg) {
                        // ------------------------------------------------------------------
                        case "createAccount" :
                            String owner = in.readUTF();
                            String id = bank.createAccount(owner);
                            System.out.println("[Server]createAccount: " + owner);
                            System.out.println("[Server]id : " + id);
                            this.out.writeUTF("" + id);
                            // this.out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "transfer":
                            ObjectInputStream accountInputStream = new ObjectInputStream(in);
                            bank.Account from = (bank.Account)accountInputStream.readObject();
                            bank.Account to   = (bank.Account)accountInputStream.readObject();
                            Double amount = in.readDouble();
                            System.out.println("from.isActive: " + from.isActive() + ", to.isActive: " + to.isActive());
                            bank.transfer(from, to, amount);
                            break;
                        // ------------------------------------------------------------------
                        case "getAccount" :
                            System.out.println("[Server]Get account.");
                            ObjectOutputStream accountOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            DataInputStream in  = new DataInputStream(socket.getInputStream());
                            accountOutputStream.writeObject(bank.getAccount(in.readUTF()));
                            break;
                        // ------------------------------------------------------------------
                        case "getAccountNumbers" :
                            System.out.println("[Server]Get account numbers.");
                            ObjectOutputStream accountNumbersOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            accountNumbersOutputStream.writeObject(bank.getAccountNumbers());
                            break;
                        // ------------------------------------------------------------------
                        case "closeAccount" :
                            System.out.println("[Server]Close account.");
                            String closeAccount = this.in.readUTF();
                            if(bank.getAccount(closeAccount).isActive()) {
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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InactiveException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
