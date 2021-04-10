package channels;

import macros.Macros;
import messageManager.*;
import peer.Peer;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;
import java.util.concurrent.Executors;

public class MCChannel extends Channel {
    private String desiredFileId;

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
                peer.Peer.scheduledThreadPoolExecutor.execute(new Removed(data, this.desiredFileId));
                break;
            case "STORED":
                peer.Peer.scheduledThreadPoolExecutor.execute(new Stored(data, this.desiredFileId));
                break;
            case "RECEIVED_DELETE":
                Peer.scheduledThreadPoolExecutor.execute(new ReceivedDelete(data, this.desiredFileId));
                break;
            case "SPECIFIC_DELETE":
                Peer.scheduledThreadPoolExecutor.execute(new ReceivedSpecificDelete(data));
                break;
            case "ON":
                Peer.scheduledThreadPoolExecutor.execute(new ReceivedON(data));
                break;
            default:
                System.err.println("MC Channel message type error:" + msgType);
        }
    }

    public void setDesiredFileId(String desiredFileId) {
        this.desiredFileId = desiredFileId;
    }
}
