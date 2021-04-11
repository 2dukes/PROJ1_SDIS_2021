package responseManager;

import peer.Peer;

import java.io.IOException;
import java.net.ServerSocket;

public class SendTCPPorts implements Runnable {
    private String version;
    private String fileId;
    private int port;

    public SendTCPPorts(String version, String fileId, int port) {
        this.version = version;
        this.fileId = fileId;
        this.port = port;
    }

    @Override
    public void run() {
        // <version> TCP_PORT <senderId> <fileId> <portNumber> <CRLF><CRLF> - Restore Enhancement
        String messageStr = this.version + " TCP_PORT " + Peer.id + " " + this.fileId + " " + this.port + "\r\n\r\n";

        for (int i = 0; i < 5; i++) {
            Peer.mdrChannel.send(messageStr.getBytes());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static int findAvailablePort() {
        int port = 0;
        while (port <= 0) {
            // For ServerSocket port number 0 means that the port number is automatically allocated.
            try (ServerSocket socket = new ServerSocket(0)) {
                // Disable timeout and reuse address after closing the socket.
                socket.setReuseAddress(true);
                port = socket.getLocalPort();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return port;
    }
}
