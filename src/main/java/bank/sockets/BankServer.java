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

        /*
        try(ServerSocket server = new ServerSocket(serverPort))  {
            System.out.println("Started server");
            while(true) {
                Socket s = server.accept();
                Thread t = new Thread(new BankHandler(s, bank));
                t.start();
            }
        }
        */

        try (ServerSocket server = new ServerSocket(serverPort)) {
            System.out.println("Startet Bank Server on port " + server.getLocalPort());
            while (true) {

                Socket s = server.accept();
                Thread t = new Thread(() -> {
                    try(s) {
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());
                        out.writeUTF("Server connected.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

                /*
                // Return a new socket when connection is established.
                try (Socket s = server.accept()) {
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    out.writeUTF("Server Connection Request Confirmation.");
                    out.flush();
                    System.out.println("Done serving " + s);
                }
                */
            }
        }
    }

    private static class BankHandler implements Runnable {
        private final Socket socket;
        private BankHandler(Socket socket, bank.Bank bank) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Connection from " + socket);

            try(Socket s = socket;
                DataInputStream in = new DataInputStream(s.getInputStream());) {
                System.out.println(in.readUTF());
                System.out.println("here");
            } catch (IOException e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
        }
    }
}
