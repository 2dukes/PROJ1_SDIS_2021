package messageManager;

import peer.Peer;

public class Chunk extends MessageManager {
    private int chunkNo;
    private String desiredFileId;

    public Chunk(byte[] data, String desiredFileId) {
        super(data);
        this.desiredFileId = desiredFileId;
    }

    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        //System.out.println("Initial=" + this.chunkNo);
        if (Peer.id != this.senderId) {
            if(!Peer.isInitiator) {
                if(peer.Peer.storage.getChunk(this.fileId, this.chunkNo) == null)
                    return;
            }
          //  System.out.println("Chunk=" + this.chunkNo);
            if(!(desiredFileId.equals(this.fileId) && Peer.isInitiator))
                return;

            peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, this.body, 0);
            Peer.storage.addRestoredChunk(chunk);

            System.out.format("RECEIVED CHUNK version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }
    }
}
