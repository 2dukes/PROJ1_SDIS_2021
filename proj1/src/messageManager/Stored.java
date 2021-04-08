package messageManager;

import peer.Peer;

public class Stored extends MessageManager {
    private int chunkNo;
    private String desiredFileId;

    public Stored(byte[] data, String desiredFileId) {
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
        String chunkKey = this.fileId + " " + this.chunkNo;
        Peer.storage.incrementStoredMessage(chunkKey);

        if(Peer.id != this.senderId) {
            if(this.version.equals("2.0") && Peer.isInitiator && this.desiredFileId != null && this.desiredFileId.equals(this.fileId))
                Peer.storage.addPeerBackingUp(this.fileId, this.senderId);


            Peer.storage.incrementChunkReplicationDeg(chunkKey);

            System.out.format("RECEIVED STORED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }

    }
}
