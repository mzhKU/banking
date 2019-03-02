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
    public static void main(String args[]) throws IOException, ClassNotFoundException {

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
                            this.out.flush();
                            break;
                        // ------------------------------------------------------------------
                        case "transfer":
                            ObjectInputStream accountInputStream = new ObjectInputStream(in);
                            try {
                                bank.Account from = (bank.Account)accountInputStream.readObject();
                                bank.Account to   = (bank.Account)accountInputStream.readObject();
                                Double amount = in.readDouble();
                                try {
                                    bank.transfer(from, to, amount);
                                } catch (InactiveException e) {
                                    e.printStackTrace();
                                } catch (OverdrawException e) {
                                    e.printStackTrace();
                                }
                            } catch (ClassNotFoundException c) {
                                throw new ClassNotFoundException();
                            }
                            break;
                        // ------------------------------------------------------------------
                        case "getAccount" :
                            System.out.println("[Server]Get Account.");
                            ObjectOutputStream accountOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            DataInputStream in  = new DataInputStream(socket.getInputStream());
                            accountOutputStream.writeObject(bank.getAccount(in.readUTF()));
                            break;
                        case "getAccountNumbers" :
                            System.out.println("[Server]Get Account Numbers.");
                            ObjectOutputStream accountNumbersOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            accountNumbersOutputStream.writeObject(bank.getAccountNumbers());
                            break;
                        // ------------------------------------------------------------------
                        default:
                            System.out.println("No instruction");
                            break;
                        // ------------------------------------------------------------------
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
