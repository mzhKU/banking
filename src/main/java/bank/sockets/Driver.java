package bank.sockets;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

// args[]:                      0         1
//         banks.sockets.Driver localhost 1234
public class Driver implements bank.BankDriver {

    private Bank bank;
    private int clientport;
    private Socket s;
    DataInputStream in;
    DataOutputStream out;

    @Override
    public void connect(String[] args) throws IOException {
        String host = args[0];
        int    port = Integer.valueOf(args[1]);
        s = new Socket(host, port);

        System.out.println("connecting to " + host + ":" + port);
        System.out.println("connected to " + s.getRemoteSocketAddress());

        in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

        String response = in.readUTF();
        System.out.println("Client received: " + response);
        /*
        */

        bank = new SocketBank();
    }


    @Override
    public Bank getBank() {
        return bank;
    }

    class SocketBank implements bank.Bank {

        @Override
        public String createAccount(String owner) throws IOException {
            // out.writeUTF("createAccount:" + owner);
            // out.flush();
            // String res = in.readUTF();
            return "createAccount:" + owner;
        }

        @Override
        public boolean closeAccount(String number) throws IOException {
            return false;
        }

        @Override
        public Set<String> getAccountNumbers() throws IOException {
            return new TreeSet<String>();
        }

        @Override
        public Account getAccount(String number) throws IOException {
            return null;
        }

        @Override
        public void transfer(Account a, Account b, double amount) throws IOException, IllegalArgumentException, OverdrawException, InactiveException {

        }
    }

    @Override
    public void disconnect() throws IOException {
        s.close();
        bank = null;
        System.out.println("disconnected.");
    }

    public Driver() {}

    public Driver(String[] config) {
        this.clientport = Integer.valueOf(config[1]);
    }
}
