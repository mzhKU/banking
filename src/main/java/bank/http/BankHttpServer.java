package bank.http;

import bank.InactiveException;
import bank.OverdrawException;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class BankHttpServer {

    private static bank.http.Bank bank = new Bank();

    public static void main(String[] args) throws IOException {
        int port = 5555;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        setupTestBank();
        server.createContext("/bank", new BankHandler(bank)).getFilters().add(new ParameterParser());
        server.start();
        System.out.println("Server available on port " + port);
    }

    // Create two test accounts with deposit
    private static void setupTestBank() {
        String one = bank.createAccount("Thomas");
        String two = bank.createAccount("Michael");
        try {
            bank.getAccount(one).deposit(100);
            bank.getAccount(two).deposit(100);
            bank.getAccount(one).withdraw(100);
            bank.closeAccount(one);
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (InactiveException e) {
            System.out.println("Inactive");
        } catch (OverdrawException e) {
            e.printStackTrace();
        }
        System.out.println("[BankHandler:setupBank]Done");
    }

    static class ParameterParser extends Filter {

        @Override
        public String description() {
            return "Parses the requested URI for parameters";
        }

        @Override
        public void doFilter(HttpExchange exchange, Chain chain)
                throws IOException {
            parseGetParameters(exchange);
            parsePostParameters(exchange);
            chain.doFilter(exchange);
        }

        private void parseGetParameters(HttpExchange exchange)
                throws UnsupportedEncodingException {
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = exchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            exchange.setAttribute("parameters", parameters);
        }

        private void parsePostParameters(HttpExchange exchange)
                throws IOException {
            if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = (Map<String, Object>) exchange
                        .getAttribute("parameters");
                InputStreamReader isr = new InputStreamReader(
                        exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                parseQuery(query, parameters);
            }
        }

        @SuppressWarnings("unchecked")
        public static void parseQuery(String query,
                                      Map<String, Object> parameters)
                throws UnsupportedEncodingException {
            if (query != null) {
                StringTokenizer st = new StringTokenizer(query, "&");
                while (st.hasMoreTokens()) {
                    String keyValue = st.nextToken();
                    StringTokenizer st2 = new StringTokenizer(keyValue, "=");
                    String key = null;
                    String value = "";
                    if (st2.hasMoreTokens()) {
                        key = st2.nextToken();
                        key = URLDecoder.decode(key, "UTF-8");
                    }

                    if (st2.hasMoreTokens()) {
                        value = st2.nextToken();
                        value = URLDecoder.decode(value, "UTF-8");
                    }

                    if (parameters.containsKey(key)) {
                        Object o = parameters.get(key);
                        if (o instanceof List) {
                            List<String> values = (List<String>) o;
                            values.add(value);
                        } else if (o instanceof String) {
                            List<String> values = new ArrayList<String>();
                            values.add((String) o);
                            values.add(value);
                            parameters.put(key, values);
                        }
                    } else {
                        parameters.put(key, value);
                    }
                }
            }
        }
    }
}
