package bank.websockets;

import bank.local.Driver;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint(
        value = "/ws",
        decoders = RequestDecoder.class,
        encoders = RequestEncoder.class
)
public class WsServer {
    private static Driver.Bank bank;
    private static final List<Session> sessions = new CopyOnWriteArrayList();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        sessions.remove(session);
    }

    @OnMessage
    public Command onMesage(Command cmd) {
        return cmd.execute(bank);
    }

    public static void main(String[] args) throws Exception {
        bank = new BankImpl(new bank.local.Driver.Bank(), id -> notifyListeners(id));
        Server server = new Server("localhost", 2222, "/bank", null, WsServer.class);
        server.start();
    }

    public static void notifyListeners(String id) {
        for(Session s : sessions) {
            try {
                s.getBasicRemote().sendText(id);
            } catch (Exception e) {
                sessions.remove(s);
            }
        }
    }
}
