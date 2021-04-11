package messageManager;

import peer.Peer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Removed extends MessageManager {
    private int chunkNo;
    private String desiredFileId;

    public Removed(byte[] data, String desiredFileId) {
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
            if (this.version.equals("2.0")) {
                Peer.storage.removePeerBackingUp(this.fileId, this.senderId);
                if(Peer.storage.getPeersBackingUp().get(this.fileId).size() == 0)
                    Peer.storage.deleteFileToRemove(this.fileId);
            }

            peer.Chunk chunk = Peer.storage.getChunk(this.fileId, this.chunkNo);
            if (chunk == null)
                return;
            String chunkKey = this.fileId + " " + this.chunkNo;
            Peer.storage.decrementChunkReplicationDeg(chunkKey);
            Peer.storage.deleteRemovedPutChunk(chunk.getKey());
            Peer.storage.decrementStoredMessage(chunkKey);

            if (chunk.getCurrentReplicationDegree() < chunk.getDesiredReplicationDegree()) {
                Peer.scheduledThreadPoolExecutor.schedule(new manageThreads.RemovedBackup(this.version, this.fileId,
                                this.chunkNo, chunk.getDesiredReplicationDegree(), chunk.getData()),
                        new Random().nextInt(401), TimeUnit.MILLISECONDS);
            }

            System.out.format("RECEIVED REMOVED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }
    }
}
