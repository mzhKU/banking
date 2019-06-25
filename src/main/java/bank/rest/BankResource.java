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
        StringBuffer resp = new StringBuffer();

        resp.append("<body><h1>Accounts</h1>");
        for(String a : bank.getAccountNumbers()) {
            resp.append("<a href=" + uriInfo.getAbsolutePathBuilder().path(a).build() + ">Account: ");
            resp.append(bank.getAccount(a).getNumber());
            resp.append(" of " + bank.getAccount(a).getOwner());
            resp.append("</a><br />");
        }
        resp.append("</body>");
        return Response.ok(resp.toString()).build();
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
    @Path("{id}")
    public Response getAccountInfo(@PathParam("id") int id) {
        StringBuffer resp = new StringBuffer();

        try {
            String accountHolderName = bank.getAccount(Integer.valueOf(id).toString()).getOwner();

            resp.append("<body><h1>");
            resp.append("<h2>Account Number</h2>");
            resp.append(id);
            resp.append("<h2>Account Holder</h2>");
            resp.append(accountHolderName);
            resp.append("</h1></body>");
            return Response.ok(resp.toString()).build();

        } catch (IOException e) {
            return Response.serverError().build();
        }
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
