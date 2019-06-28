package bank.websockets;

import bank.local.Driver;

import java.io.Serializable;

public abstract class Command implements Serializable {
    private Exception e;
    public void setException(Exception e) { this.e = e; }
    public Exception getException() { return e; }
    public abstract Command execute(Driver.Bank b);
}
