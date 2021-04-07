package channels;

import java.io.IOException;
import java.net.*;

public abstract class Channel implements Runnable {
    private DatagramSocket socket;
    private InetAddress destination;
    private String IP;
    private int port;
    private DatagramPacket packet;

    public Channel(String IP, int port) throws SocketException, UnknownHostException {
        this.IP = IP;
        this.port = port;
        try {
            this.destination = InetAddress.getByName(this.IP);
            this.socket = new DatagramSocket();
        } catch (Exception err) {
            System.err.println(err.getMessage());
        }
    }

    public synchronized void send(byte[] outbuf) {
        try {
            DatagramPacket packet = new DatagramPacket(outbuf, outbuf.length, destination, this.port);
            socket.send(packet);
        } catch(IOException err)   {
            System.err.println(err.getMessage());
        }
    }

    public abstract void handleMessageType(byte[] data);

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
                this.packet = new DatagramPacket(inbuf, inbuf.length);
                multicastSocket.receive(this.packet);


                byte[] data = new byte[this.packet.getLength()];
                System.arraycopy(this.packet.getData(), this.packet.getOffset(), data, 0, this.packet.getLength());

                handleMessageType(data);

                // Executors.newScheduledThreadPool(Macros.NUM_THREADS).execute(new MessageManagerBackup(packet.getData()));
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

    public DatagramPacket getPacket() { return packet; }
}
