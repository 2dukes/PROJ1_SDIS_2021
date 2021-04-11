package responseManager;

import peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SendStored implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;
    private String chunkKey;
    private int desiredReplicationDeg;
    private byte[] body;

    public SendStored(String version, String fileId, int chunkNo, int desiredReplicationDeg, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkKey = this.fileId + " " + this.chunkNo;
        this.desiredReplicationDeg = desiredReplicationDeg;
        this.body = body;
    }

    @Override
    public void run() {
        if (Peer.version.equals("2.0")) { // Backup Enhancement
            if (Peer.storage.getNumberOfStoredChunks().containsKey(this.chunkKey)) {
                if (Peer.storage.getNumberOfStoredChunks().get(this.chunkKey) >= this.desiredReplicationDeg)
                    return;
            }
        }
        sendMessage();
    }

    private void sendMessage() {
        peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, this.body, this.desiredReplicationDeg);

        Peer.storage.putChunk(chunk);

        createChunkFile(chunk.getData());
        Peer.storage.incrementChunkReplicationDeg(this.chunkKey);
        Peer.storage.incrementStoredMessage(this.chunkKey);


        System.out.format("SENT STORED version=%s senderId=%s fileId=%s chunkNo=%s \n",
                Peer.version, Peer.id, this.fileId, this.chunkNo);

        String messageStr = Peer.version + " STORED " + Peer.id + " " + this.fileId + " "
                + this.chunkNo + "\r\n\r\n";

        byte[] message = messageStr.getBytes();

        Peer.mcChannel.send(message);
    }

    public void createChunkFile(byte[] data) {
        try {
            String fileName = "../../resources/peers/" + Peer.id + "/chunks/" + this.chunkKey;

            File f = new File(fileName);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            System.out.println("Creating file....");

            FileOutputStream file = new FileOutputStream(fileName);
            file.write(data, 0, data.length);
            file.close();

            System.out.println("DONE");
        } catch (IOException e) {
            System.err.println("Exception was caught: " + e.getMessage());
        }
    }
}
