package channels;

import messageManager.Delete;
import messageManager.GetChunk;
import messageManager.PutChunk;
import messageManager.Stored;

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
    public void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split(" ")[1];
        switch (msgType) {
            case "GETCHUNK":
                Executors.newScheduledThreadPool(150).execute(new GetChunk(data));
                break;
            case "DELETE":
                Executors.newScheduledThreadPool(150).execute(new Delete(data));
                break;
            case "REMOVED":
                // ...
                break;
            case "STORED":
                Executors.newScheduledThreadPool(150).execute(new Stored(data));
                break;
            default:
                System.err.println("MC Channel message type error:" + msgType);
        }
    }
}
