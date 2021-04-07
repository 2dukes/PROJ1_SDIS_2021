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
                peer.Peer.scheduledThreadPoolExecutor.execute(new GetChunk(data, this.getPacket().getAddress()));
                break;
            case "DELETE":
                peer.Peer.scheduledThreadPoolExecutor.execute(new Delete(data));
                break;
            case "REMOVED":
                peer.Peer.scheduledThreadPoolExecutor.execute(new Removed(data));
                break;
            case "STORED":
                peer.Peer.scheduledThreadPoolExecutor.execute(new Stored(data));
                break;
            default:
                System.err.println("MC Channel message type error:" + msgType);
        }
    }
}
