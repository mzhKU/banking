package stuff;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Localhost {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        System.out.printf("%s/%s/%n",
                address.getHostName(),
                address.getHostAddress());
    }
}
