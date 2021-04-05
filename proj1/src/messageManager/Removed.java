package messageManager;

import peer.Peer;

import static macros.Macros.INITIATOR_ID;

public class Removed extends MessageManager {
    private int chunkNo;

    public Removed(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId) {
            if(Peer.id == INITIATOR_ID)
                return;

            Peer.storage.decrementChunkReplicationDeg(this.fileId + " " + this.chunkNo);

            System.out.format("RECEIVED REMOVED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);

        }
    }
}
