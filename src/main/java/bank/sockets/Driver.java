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

    private DataOutputStream out;
    private DataInputStream  in;
    private Socket           s;
    private Bank             bank;
    private int              clientport;

    @Override
    public void connect(String[] args) throws IOException {
        String host = args[0];
        int    port = Integer.valueOf(args[1]);
        s           = new Socket(host, port);

        System.out.println("connecting to " + host + ":" + port);
        System.out.println("connected to "  + s.getRemoteSocketAddress());

        // BufferedStream(InputStream): Bytes
        // DataStream                 : binary
        out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
        in = new DataInputStream(s.getInputStream());

        bank = new SocketBank();
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    class SocketBank implements bank.Bank {

        @Override
        public String createAccount(String owner) throws IOException {
            //out.writeUTF("createAccount:" + owner);
            out.writeUTF("createAccount");
            out.flush();
            System.out.println("[Client]New owner: " + owner);
            out.writeUTF(owner);
            out.flush();
            String createdAccountNumber = in.readUTF();
            System.out.println("[Client]Created Account Number: " + createdAccountNumber);
            return createdAccountNumber;
        }

        @Override
        public boolean closeAccount(String number) throws IOException {
            out.writeUTF("closeAccount");
            out.flush();
            out.writeUTF(number);
            out.flush();
            Boolean close = null;
            try {
                close = in.readBoolean();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
                return false;
            }
            return close;
        }

        @Override
        public Set<String> getAccountNumbers() throws IOException {
            out.writeUTF("getAccountNumbers");
            out.flush();

            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            Set<String> accountNumbers = null;
            try {
                accountNumbers = (Set<String>)in.readObject();
                System.out.println("[Client]Account numbers received: " + accountNumbers);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return accountNumbers;
        }

        @Override
        public Account getAccount(String number) throws IOException {
            out.writeUTF("getAccount");
            out.flush();
            out.writeUTF(number);
            out.flush();
            DataInputStream accountInputStream = new DataInputStream(s.getInputStream());

            String accountOwner = accountInputStream.readUTF();
            String numberIn  = accountInputStream.readUTF();

            // Return a proxy
            return new bank.local.Driver.Account(accountOwner, numberIn);
        }

        @Override
        public void transfer(Account a, Account b, double amount) throws IOException, IllegalArgumentException {
            out.writeUTF("transfer");
            out.flush();

            out.writeUTF(a.getNumber());
            out.flush();
            out.writeUTF(b.getNumber());
            out.flush();

            /*
            accountOutputStream.writeObject(a);
            accountOutputStream.flush();
            accountOutputStream.writeObject(b);
            accountOutputStream.flush();
            */

            out.writeDouble(amount);
            out.flush();
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
