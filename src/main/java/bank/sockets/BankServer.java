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

        bank.Bank bank = new bank.local.Driver.Bank();

        try (ServerSocket server = new ServerSocket(serverPort)) {
            System.out.println("Startet Bank Server on port " + server.getLocalPort());
            while (true) {
                Socket s = server.accept();
                Thread t = new Thread(new BankHandler(s, bank));
                t.start();
            }
        }
    }

    private static class BankHandler implements Runnable {

        private final Socket s;
        private bank.Bank bank;

        private BankHandler(Socket socket, bank.Bank bank) {
            this.s = socket;
            this.bank = bank;
        }

        @Override
        public void run() {
            System.out.println("Connection from " + s);
            try(this.s) {
                // Todo: How to handle the incoming instructions?
            } catch (IOException e) {
                System.out.println(e);
            }
       }
    }
}
