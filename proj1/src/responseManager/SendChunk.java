package responseManager;

import macros.Macros;
import peer.Peer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SendChunk implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;
    private byte[] data;
    private InetAddress IP;
    public SendChunk(String version, String fileId, int chunkNo, byte[] data, InetAddress IP) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.data = data;
        this.IP = IP;
    }

    @Override
    public void run() {
        try {
            if(Peer.storage.chunkAlreadyRestored(this.fileId, this.chunkNo)) {
                Peer.storage.deleteRestoredChunks(this.fileId, this.chunkNo);
                return;
            }

            Peer.storage.deleteRestoredChunks(this.fileId, this.chunkNo);

            String messageStr = this.version + " CHUNK " + Peer.id + " " + this.fileId + " "
                    + this.chunkNo + "\r\n\r\n";

            byte[] header = messageStr.getBytes();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(this.data);

            byte[] message = outputStream.toByteArray();

            if(this.version.equals("2.0")) {
                // TCP connection with Initiator

                Peer.semaphore.acquire();
                try {
                    int TCP_Port = Peer.storage.getFilePort(this.fileId);
                    Socket clientSocket = new Socket(this.IP, TCP_Port);
                    // https://stackoverflow.com/questions/2878867/how-to-send-an-array-of-bytes-over-a-tcp-connection-java-programming
                    DataOutputStream outBuf = new DataOutputStream(clientSocket.getOutputStream());
                    outBuf.writeInt(message.length);
                    outBuf.write(message, 0, message.length);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    Peer.semaphore.release();
                }
            }


            Peer.mdrChannel.send(message);

            System.out.format("SENT CHUNK version=%s senderId=%s fileId=%s chunkNo=%s \n",
                    this.version, Peer.id, this.fileId, this.chunkNo);

        } catch(IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

}
