package bank.rest;

/*
Resource /bank
curl -X GET -i http://localhost:14999/bank
*/

import bank.InactiveException;
import com.sun.research.ws.wadl.Request;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

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

    @GET
    @Path("{id}")
    public Response getAccountInfo(@PathParam("id") int id) {
        StringBuffer resp = new StringBuffer();
        resp.append("<body><h1>");
        resp.append(id);
        resp.append("</h1></body>");
        return Response.ok(resp.toString()).build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccountNumbers(@Context UriInfo uriInfo) throws IOException {
        StringBuffer response = new StringBuffer();


        for(String a : bank.getAccountNumbers()) {
            response.append(uriInfo.getAbsolutePathBuilder().path(a).build() + "\n");
        }

        System.out.println(response.toString());

        return response.toString();
    }


    @GET
    @Produces("application/json")
    public Bank getJson() { return this.bank; }


    @GET
    @Produces("application/json")
    @Path("{id}")
    public String getAccountHolderName(@PathParam("id") String path, @Context Request r) throws IOException {
        return this.bank.getAccount(path).getOwner();
    }
}
