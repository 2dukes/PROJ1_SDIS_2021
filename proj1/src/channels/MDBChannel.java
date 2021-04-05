package channels;

import messageManager.PutChunk;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MDBChannel extends Channel {

    public MDBChannel(String IP, int port) throws SocketException, UnknownHostException {
        super(IP, port);
    }

    @Override
    public void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split("\\s+")[1];
        switch (msgType) {
            case "PUTCHUNK":
                Executors.newScheduledThreadPool(150).execute(new PutChunk(data));
                break;
            default:
                System.err.println("MDB Channel message type error:" + msgType);
        }
    }
}
