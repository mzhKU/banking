package bank.sockets;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {
    public static void main(String args[]) throws IOException {

        // Port is defined server side by the service provider
        // It has to be known by the client, to which server we're
        // connecting. The port is *not* provided from the client
        // to the server.
        int serverPort = 1234;

        // bank.Bank bank = new bank.local.Driver.Bank();

        ServerSocket server = new ServerSocket(serverPort);
        System.out.println("Startet Bank Server on port " + server.getLocalPort());
        while(true) {
            Socket s = server.accept();
            Thread t = new Thread(new BankHandler(s));
            t.start();
        }
    }

    private static class BankHandler implements Runnable {

        private Socket socket;
        private DataOutputStream out;
        private DataInputStream in;
        private bank.Bank bank;

        public BankHandler(Socket socket) {
            this.socket = socket;
            this.bank = new bank.local.Driver.Bank();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    this.in = new DataInputStream(socket.getInputStream());
                    this.out = new DataOutputStream(socket.getOutputStream());
                    String arg = in.readUTF();
                    switch(arg) {
                        case "createAccount" :
                            String owner = in.readUTF();
                            String id = bank.createAccount(owner);
                            this.out.writeUTF("" + id);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
