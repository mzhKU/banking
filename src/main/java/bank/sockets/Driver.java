package bank.sockets;

import bank.Bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// args[]:                      0         1
//         banks.sockets.Driver localhost 1234
public class Driver implements bank.BankDriver {

    private int clientport;
    private Bank bank;

    @Override
    public void connect(String[] args) throws IOException {
        String host = args[0];
        int    port = Integer.valueOf(args[1]);
        try(ServerSocket server = new ServerSocket(port)) {
            System.out.println("Started server on port " + port);

            while(true) {
                try(Socket s = server.accept()) {

                }
            }


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public Bank getBank() {
        String host = "localhost";
        int    port = this.clientport;

        return null;
    }

    public Driver() {}

    public Driver(String[] config) {
        this.clientport = Integer.valueOf(config[1]);
    }
}
