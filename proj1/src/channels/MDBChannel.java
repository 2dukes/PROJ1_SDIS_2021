package channels;

import messageManager.PutChunk;
import peer.Peer;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MDBChannel extends Channel {

    public MDBChannel(String IP, int port) throws SocketException, UnknownHostException {
        super(IP, port);
    }

    @Override
    public synchronized void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split("\\s+")[1];
        if ("PUTCHUNK".equals(msgType))
            Peer.scheduledThreadPoolExecutor.execute(new PutChunk(data));
        else
            System.err.println("MDB Channel message type error:" + msgType);
    }
}