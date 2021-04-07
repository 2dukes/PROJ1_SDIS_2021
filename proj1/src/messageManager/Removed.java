package messageManager;

import peer.Peer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
            peer.Chunk chunk = Peer.storage.getChunk(this.fileId, this.chunkNo);
            if(chunk == null)
                return;

            Peer.storage.decrementChunkReplicationDeg(this.fileId + " " + this.chunkNo);
            Peer.storage.deleteRemovedPutChunk(chunk.getKey());

            if (chunk.getCurrentReplicationDegree() < chunk.getDesiredReplicationDegree()) {
                Peer.scheduledThreadPoolExecutor.schedule(new manageThreads.RemovedBackup(this.fileId,
                        this.chunkNo, chunk.getDesiredReplicationDegree(), chunk.getData()),
                        new Random().nextInt(401), TimeUnit.MILLISECONDS);
            }

            System.out.format("RECEIVED REMOVED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);
        }
    }
}
