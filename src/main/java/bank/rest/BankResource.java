package bank.rest;

/*
Resource /bank
curl -X GET -i http://localhost:14999/bank
*/

import bank.InactiveException;
import com.sun.research.ws.wadl.Request;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

import static bank.local.Driver.Bank;


// Who instantiates the resource?
@Singleton
@Path("/accounts")
public class BankResource {

    private final Bank bank;

    public BankResource() {
        this.bank = new Bank();
        setupTestBank(bank);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getAccountNumbers(@Context UriInfo uriInfo) throws IOException {
        StringBuffer page = new StringBuffer();

        page.append("<body><h1>Accounts</h1>");
        for(String a : bank.getAccountNumbers()) {
            URI accountPath = uriInfo.getAbsolutePathBuilder().path(a).build();

            page.append("<a href=" + accountPath + ">Account ");
            page.append(bank.getAccount(a).getNumber() + ": ");
            page.append(bank.getAccount(a).getOwner());
        }

        page.append("<form action=\"/bank/accounts\" method=\"POST\">");
        page.append("<input type=\"text\" placeholder=\"New Client Name\" name=\"owner\">");
        page.append("<br />");
        page.append("<input type=\"submit\" value=\"Create new Account\"");
        page.append("</form>");
        page.append("</body>");

        return Response.ok(page.toString()).build();
    }

    @GET
    @Path("{id}")
    public Response getAccountInfo(@PathParam("id") int id) {
        StringBuffer resp = new StringBuffer();

        try {
            String accountHolderName = bank.getAccount(Integer.valueOf(id).toString()).getOwner();
            double balance = bank.getAccount(Integer.valueOf(id).toString()).getBalance();

            resp.append("<body>");
            resp.append("<h2>Account Number</h2>");
            resp.append(id);
            resp.append("<h2>Account Holder</h2>");
            resp.append(accountHolderName);
            resp.append("<h2>Balance</h2>");
            resp.append(Double.valueOf(balance).toString());

            resp.append("</body>");
            return Response.ok(resp.toString()).build();

        } catch (IOException e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response create(
            @Context UriInfo uriInfo,
            @FormParam("owner") String owner) throws IOException {
        String id = bank.createAccount(owner);
        URI location = uriInfo.getRequestUriBuilder().path(id).build();
        return Response.created(location).build();
    }




    @GET
    @Produces("application/json")
    @Path("{id}")
    public String getAccountHolderName(@PathParam("id") String path, @Context Request r) throws IOException {
        return this.bank.getAccount(path).getOwner();
    }



    public static void setupTestBank(Bank bank) {
        String one = bank.createAccount("Thomas");
        String two = bank.createAccount("Michael");
        try {
            bank.getAccount(one).deposit(100);
            bank.getAccount(two).deposit(100);
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (InactiveException e) {
            System.out.println("Inactive");
        }
        System.out.println("[BankHandler:setupBank]Done");
    }
}
