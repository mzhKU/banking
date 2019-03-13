package bank.http;

import bank.InactiveException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BankHandler implements HttpHandler {

    private String RESPONSE = "";
    private String INDEX = "";

    private static bank.http.Bank bank;

    public BankHandler(bank.http.Bank bank) throws IOException {
        this.bank = bank;
        try {
            URI responseTemplate = this.getClass().getResource("/response.html").toURI();
            URI indexTemplate    = this.getClass().getResource("/index.html").toURI();

            StringBuilder responseBuffer = new StringBuilder();
            StringBuilder indexBuffer    = new StringBuilder();

            Files.lines(Paths.get(responseTemplate)).forEach(responseBuffer::append);
            Files.lines(Paths.get(indexTemplate)).forEach(indexBuffer::append);

            RESPONSE = responseBuffer.toString();
            INDEX    = indexBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        StringBuilder buf = new StringBuilder();
        if(exchange.getRequestURI().getPath().endsWith("bank")) {
            buf.append(INDEX);
        }
        if(exchange.getRequestURI().getPath().endsWith("getAccount")) {
            buf.append(String.format(RESPONSE, "getAccount"));
        }
        response = buf.toString();

        exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(Charset.forName("UTF-8")));
        os.close();
    }

    private void setupBank() {
        String id = bank.createAccount("thomas");
        try {
            bank.getAccount(id).deposit(100);
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (InactiveException e) {
            System.out.println("Inactive");
        }
        System.out.println("[BankHandler:setupBank]Done");
    }





}
