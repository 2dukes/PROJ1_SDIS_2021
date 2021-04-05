package channels;

import messageManager.Chunk;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

public class MDRChannel extends Channel {

    public MDRChannel(String IP, int port) throws SocketException, UnknownHostException {
        super(IP, port);
    }

    @Override
    public void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split("\\s+")[1];
        switch (msgType) {
            case "CHUNK":
                Executors.newScheduledThreadPool(150).execute(new Chunk(data));
                break;
            default:
                System.err.println("MDR Channel message type error:" + msgType);
        }
    }
}
