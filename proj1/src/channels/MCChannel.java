package channels;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;

public class MCChannel implements Runnable {
    private DatagramSocket socket;
    private InetAddress destination;
    private String IP;
    private int port;

    public MCChannel(String IP, int port) throws SocketException, UnknownHostException {
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

    // GETCHUNK | DELETE | REMOVED | STORED
    public void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split(" ")[1];
        switch (msgType) {
            case "GETCHUNK":
                // ...
                break;
            case "DELETE":
                // ...
                break;
            case "REMOVED":
                // ...
                break;
            case "STORED":
                System.out.println("Received stored!!!");
                break;
            default:
                System.err.println("MC Channel message type error:" + msgType);
        }
    }

    @Override
    public void run() {
        byte[] inbuf = new byte[70000];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(this.port); // TODO: fix bug permission denied
            multicastSocket.joinGroup(this.destination);

            // NetworkInterface netInterface = NetworkInterface.getByName("wlp2s0");
            // SocketAddress sockAdr = new InetSocketAddress(this.IP, this.port);
            //multicastSocket.joinGroup(sockAdr, netInterface);
            while(true) {
                DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
                multicastSocket.receive(packet);

               /* System.out.println(packet);
                System.out.println(packet.getLength());*/

                handleMessageType(packet.getData());

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
