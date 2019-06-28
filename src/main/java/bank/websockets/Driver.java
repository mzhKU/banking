package bank.websockets;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;

public class Driver implements bank.BankDriver2 {
    private bank.Bank bank;
    private final SynchronousQueue<Command> q = new SynchronousQueue();
    private Session session;

    // binary message
    @OnMessage
    public void onMessage(Command cmd) throws InterruptedException {
        q.put(cmd);
    }

    // text message
    @OnMessage
    public void onMessage(String id) {
        for(UpdateHandler h : listeners) {
            try { h.accountChanged(id); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void connect(String[] args) throws IOException {
        try {
            URI uri = new URI("ws://" + args[0] + "/bank/ws");
            System.out.println("Connecting to " + uri);
            ClientManager client = ClientManager.createClient();
            session = client.connectToServer(this, uri);
        } catch (URISyntaxException | DeploymentException e) {
            throw new IOException(e);
        }

        bank = new bank.commands.CommandBank(cmd -> {
            try {
                session.getBasicRemote().sendObject(cmd);
                return q.take();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void disconnect() throws IOException {
        bank = null;
        session.close();
        System.out.println("Disconnected.");
    }

    public bank.Bank getBank() {
        return bank;
    }

    private final List<UpdateHandler> listeners = new CopyOnWriteArrayList();

    @Override
    public void registerUpdateHandler(UpdateHandler handler) {
        listeners.add(handler);
    }
}
