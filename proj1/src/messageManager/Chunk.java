package messageManager;

import peer.Peer;

import java.sql.SQLOutput;

public class Chunk extends MessageManager {
    private int chunkNo;
    private String desiredFileId;

    public Chunk(byte[] data, String desiredFileId) {
        super(data);
        this.desiredFileId = desiredFileId;
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        //System.out.println("Initial=" + this.chunkNo);
        if (Peer.id != this.senderId) {
            if(this.version.equals("2.0") && desiredFileId != null && desiredFileId.equals(this.fileId)) // Restore Enhancement
                return;

            if(!Peer.isInitiator) {
                if(peer.Peer.storage.getChunk(this.fileId, this.chunkNo) == null)
                    return;
            }

          //  System.out.println("Chunk=" + this.chunkNo);
            if(desiredFileId != null && !desiredFileId.equals(this.fileId) && Peer.isInitiator)
                return;

            peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, this.body, 0);

            Peer.storage.addRestoredChunk(chunk);

            System.out.format("RECEIVED CHUNK version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }
    }
}
