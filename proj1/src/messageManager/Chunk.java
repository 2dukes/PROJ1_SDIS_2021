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
        this.fileId = this.header[3];
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId) {
            if(Peer.version.equals("2.0") && this.version.equals("2.0") && Peer.isInitiator)
                return;

            if (!Peer.isInitiator) {
                if (peer.Peer.storage.getChunk(this.fileId, this.chunkNo) == null)
                    return;
            }

            if (desiredFileId != null && !desiredFileId.equals(this.fileId) && Peer.isInitiator)
                return;

            peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, this.body, 0);

            if (Peer.storage.addRestoredChunk(chunk))
                Peer.storage.incrementNumberOfReceivedChunks();


            System.out.format("RECEIVED CHUNK version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }
    }
}
