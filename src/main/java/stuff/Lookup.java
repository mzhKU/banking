package stuff;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class Lookup {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(args[0]);
        System.out.printf("Looking up %s -> %s/%s%n",
                args[0],
                address.getHostName(),
                address.getHostAddress());
    }
}

