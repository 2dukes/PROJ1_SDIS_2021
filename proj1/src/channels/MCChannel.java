package channels;

import messageManager.*;
import peer.Peer;

import java.net.SocketException;
import java.net.UnknownHostException;

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
            case "GETCHUNK" -> Peer.scheduledThreadPoolExecutor.execute(new GetChunk(data, this.getPacket().getAddress()));
            case "DELETE" -> Peer.scheduledThreadPoolExecutor.execute(new Delete(data));
            case "REMOVED" -> Peer.scheduledThreadPoolExecutor.execute(new Removed(data, this.desiredFileId));
            case "STORED" -> Peer.scheduledThreadPoolExecutor.execute(new Stored(data, this.desiredFileId));
            case "RECEIVED_DELETE" -> Peer.scheduledThreadPoolExecutor.execute(new ReceivedDelete(data, this.desiredFileId));
            case "SPECIFIC_DELETE" -> Peer.scheduledThreadPoolExecutor.execute(new ReceivedSpecificDelete(data));
            case "ON" -> Peer.scheduledThreadPoolExecutor.execute(new ReceivedON(data));
            default -> System.err.println("MC Channel message type error:" + msgType);
        }
    }

    public void setDesiredFileId(String desiredFileId) {
        this.desiredFileId = desiredFileId;
    }
}
