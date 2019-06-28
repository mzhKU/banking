package bank.websockets;

import java.io.IOException;

public interface RequestHandler {
    Command handle(Command request) throws IOException;
}
