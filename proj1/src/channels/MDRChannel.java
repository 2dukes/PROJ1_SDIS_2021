package channels;

import messageManager.Chunk;
import messageManager.TCP_Port;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MDRChannel extends Channel {
    private String desiredFileId;

    public MDRChannel(String IP, int port) throws SocketException, UnknownHostException {
        super(IP, port);
    }

    @Override
    public synchronized void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split("\\s+")[1];
        switch (msgType) {
            case "CHUNK" -> peer.Peer.scheduledThreadPoolExecutor.execute(new Chunk(data, this.desiredFileId));
            case "TCP_PORT" -> peer.Peer.scheduledThreadPoolExecutor.execute(new TCP_Port(data));
            default -> System.err.println("MDR Channel message type error:" + msgType);
        }
    }

    public void setDesiredFileId(String desiredFileId) {
        this.desiredFileId = desiredFileId;
    }

}
