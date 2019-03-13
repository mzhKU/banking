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

    private String INDEX = "";
    private static bank.http.Bank bank;

    public BankHandler(bank.http.Bank bank) throws IOException {
        this.bank = bank;
        try {
            URI uri = this.getClass().getResource("/index.html").toURI();
            StringBuilder buf = new StringBuilder();
            Files.lines(Paths.get(uri)).forEach(buf::append);
            INDEX = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        if(exchange.getRequestURI().getPath().endsWith("bank")) {
            StringBuilder buf = new StringBuilder();
            buf.append(INDEX);
            response = buf.toString();
        }

        System.out.println("[BankHandler:handle]Response: " + response);
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
