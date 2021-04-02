package channels;

import messageManager.PutChunk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;

public class MDBChannel implements Runnable {
    private DatagramSocket socket;
    private InetAddress destination;
    private String IP;
    private int port;

    public MDBChannel(String IP, int port) {
        this.IP = IP;
        this.port = port;
        try {
            this.destination = InetAddress.getByName(this.IP);
            this.socket = new DatagramSocket();
        } catch (Exception err) {
            System.err.println(err.getMessage());
        }
    }

    public void send(byte[] outbuf) {
        try {
            DatagramPacket packet = new DatagramPacket(outbuf, outbuf.length, destination, this.port);
            socket.send(packet);
        } catch(IOException err)   {
            System.err.println(err.getMessage());
        }
    }

    // [version ] [   PUTCHUNK] ...
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

    @Override
    public void run() {
        byte[] inbuf = new byte[70000];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(this.port);
            multicastSocket.joinGroup(this.destination);
            // NetworkInterface netInterface = NetworkInterface.getByName("wlp2s0");
            // SocketAddress sockAdr = new InetSocketAddress(this.IP, this.port);
            //multicastSocket.joinGroup(sockAdr, netInterface);
            while(true) {
                DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
                multicastSocket.receive(packet);

                // Executors.newScheduledThreadPool(150).execute(new MessageManagerBackup(packet.getData()));
            }

        } catch(IOException err) {
            System.err.println(err.getMessage());
        }
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }
}
