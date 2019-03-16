package bank.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class BankHandler implements HttpHandler {

    private String RESPONSE = "";
    private String INDEX = "";

    private static bank.http.Bank bank;

    public BankHandler(bank.http.Bank bank) throws IOException {
        this.bank = bank;
        setIndexTemplate();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        StringBuilder buf = new StringBuilder();
        System.out.println("[BankHandler:handle]Request URI: " + exchange.getRequestURI());
        buf.append(accountList());

        if(exchange.getRequestMethod().equals("POST")) {
            Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
            String user = (String) parameters.get("user");
            this.bank.createAccount(user);
            exchange.getResponseHeaders().add("Location", "/bank");
            exchange.sendResponseHeaders(301, -1);
            return;
        }

        response = buf.toString();
        // System.out.println(response);

        exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(Charset.forName("UTF-8")));
        os.close();
    }

    public String accountList() {
        String accountList = "";
        for (String accountNumber : bank.getAccountNumbers()) {
            try {
                accountList += "<tr>";
                accountList += "<td>" + accountNumber + "</td>";
                accountList += "<td>" + bank.getAccount(accountNumber).getBalance() + "</td>";
                accountList += "<td>";
                accountList += "<form><input type=\"number\" name=\"deposit\"><input type=\"submit\" value=\"Deposit\"></form>";
                accountList += "</td>";
                accountList += "</tr>";
                // accountList += "<li>Account Number: " + accountNumber + ", Balance: " + bank.getAccount(accountNumber).getBalance() + "</li>";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return String.format(INDEX, accountList);
    }
    public void setIndexTemplate() {
        try {
            URI indexTemplate = this.getClass().getResource("/index.html").toURI();
            StringBuilder indexBuffer = new StringBuilder();
            Files.lines(Paths.get(indexTemplate)).forEach(indexBuffer::append);
            this.INDEX = indexBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
