package bank.websockets;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class RequestEncoder implements Encoder.BinaryStream<Command> {

    @Override
    public void init(EndpointConfig config) { }

    @Override
    public void destroy() { }

    @Override
    public void encode(Command cmd, OutputStream os) throws EncodeException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(cmd);
    }
}

