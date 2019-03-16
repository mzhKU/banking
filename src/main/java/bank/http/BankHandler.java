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
            Map<String, Object> parameters = (Map<String, Object>)exchange.getAttribute("parameters");

            // System.out.println(parameters.keySet());

            if(parameters.keySet().contains("createAccount")) {
                String user = (String)parameters.get("createAccount");
                this.bank.createAccount(user);
            }

            if(parameters.keySet().contains("depositAmount")) {
                try {
                    double depositAmount  = Double.valueOf((String)parameters.get("depositAmount"));
                    String depositAccount = (String)parameters.get("depositAccount");
                    this.bank.getAccount(depositAccount).deposit(depositAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(parameters.keySet().contains("withdrawAmount")) {
                try {
                    double withdrawAmount  = Double.valueOf((String)parameters.get("withdrawAmount"));
                    String withdrawAccount = (String)parameters.get("withdrawAccount");
                    this.bank.getAccount(withdrawAccount).withdraw(withdrawAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            exchange.getResponseHeaders().add("Location", "/bank");
            exchange.sendResponseHeaders(301, -1);
            return;
        }

        response = buf.toString();

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
                accountList += "<td>" + bank.getAccount(accountNumber).getOwner() + "</td>";
                accountList += "<td>" + bank.getAccount(accountNumber).getBalance() + "</td>";

                //  Deposit
                accountList += "<td>";
                accountList += "<form action=\"/bank\" method=\"POST\">";
                accountList += "<input type=\"number\" name=\"depositAmount\">";
                accountList += "<input type=\"submit\" value=\"Deposit\">";
                accountList += "<input type=\"hidden\" value=\"" + accountNumber + "\" name=\"depositAccount\">";
                accountList += "</form>";
                accountList += "</td>";

                // Withdraw
                accountList += "<td>";
                accountList += "<form action=\"/bank\" method=\"POST\">";
                accountList += "<input type=\"number\" name=\"withdrawAmount\">";
                accountList += "<input type=\"submit\" value=\"Withdraw\">";
                accountList += "<input type=\"hidden\" value=\"" + accountNumber + "\" name=\"withdrawAccount\">";
                accountList += "</form>";
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
