package bank.rest;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Server {
    public static void main(String[] args) throws Exception {
        URI baseUri = new URI("http://localhost:14999/bank");
        // @Singleton annotations will be respected
        ResourceConfig rc = new ResourceConfig(BankResource.class);
        // Create and start the JDK HttpServer with the Jersey application
        JdkHttpServerFactory.createHttpServer(baseUri, rc);

    }
}