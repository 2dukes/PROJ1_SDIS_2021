package responseManager;

import macros.Macros;
import peer.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SendChunk implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;
    private byte[] data;

    public SendChunk(String version, String fileId, int chunkNo, byte[] data) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.data = data;
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
            Peer.mdrChannel.send(message);

            System.out.format("SENT CHUNK version=%s senderId=%s fileId=%s chunkNo=%s \n",
                    this.version, Peer.id, this.fileId, this.chunkNo);
        } catch(IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
