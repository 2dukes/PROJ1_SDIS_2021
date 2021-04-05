package channels;

import macros.Macros;
import messageManager.*;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;
import java.util.concurrent.Executors;

public class MCChannel extends Channel {

    public MCChannel(String IP, int port) throws SocketException, UnknownHostException {
        super(IP, port);
    }

    // GETCHUNK | DELETE | REMOVED | STORED
    @Override
    public synchronized void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split(" ")[1];
        switch (msgType) {
            case "GETCHUNK":
                Executors.newScheduledThreadPool(Macros.NUM_THREADS).execute(new GetChunk(data));
                break;
            case "DELETE":
                Executors.newScheduledThreadPool(Macros.NUM_THREADS).execute(new Delete(data));
                break;
            case "REMOVED":
                Executors.newScheduledThreadPool(Macros.NUM_THREADS).execute(new Removed(data));
                break;
            case "STORED":
                Executors.newScheduledThreadPool(Macros.NUM_THREADS).execute(new Stored(data));
                break;
            default:
                System.err.println("MC Channel message type error:" + msgType);
        }
    }
}
