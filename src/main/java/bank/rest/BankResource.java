package bank.rest;

/*
Resource /bank
curl -X GET -i http://localhost:14999/bank
*/

import bank.InactiveException;
import bank.OverdrawException;
import com.sun.research.ws.wadl.Request;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Singleton
@Path("/bank")
public class BankResource {

    Bank bank;

    public BankResource() {
        this.bank = new Bank();
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
