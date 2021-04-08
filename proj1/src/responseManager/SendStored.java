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
        String chunkKey = this.fileId + " " + this.chunkNo;

        if(this.version.equals("2.0")) { // Backup Enhancement
            if(Peer.storage.getNumberOfStoredChunks().containsKey(chunkKey)) {
                if (Peer.storage.getNumberOfStoredChunks().get(chunkKey) >= this.desiredReplicationDeg)
                    return;
            }
        }
        sendMessage(chunkKey);
    }

    private void sendMessage(String chunkKey) {
        peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, this.body, this.desiredReplicationDeg);

        Peer.storage.putChunk(chunk);

        createChunkFile(chunk.getData());
        Peer.storage.incrementChunkReplicationDeg(chunkKey);
        Peer.storage.incrementStoredMessage(chunkKey);


        System.out.format("SENT STORED version=%s senderId=%s fileId=%s chunkNo=%s \n",
                this.version, Peer.id, this.fileId, this.chunkNo);

        String messageStr = this.version + " STORED " + Peer.id + " " + this.fileId + " "
                + this.chunkNo + "\r\n\r\n";

        byte[] message = messageStr.getBytes();

        Peer.mcChannel.send(message);
    }

    public void createChunkFile(byte[] data) {
        try {
            String fileName = "src/files/chunks/" + Peer.id + "/" + this.chunkKey;

            File f = new File(fileName);
            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            System.out.println("Creating file....");

            FileOutputStream file = new FileOutputStream(fileName);
//          System.out.println(data.length);
            file.write(data, 0, data.length);
            file.close();

            System.out.println("DONE");
        }
        catch(IOException e) {
            System.err.println("Exception was caught: " + e.toString());
        }
    }
}
