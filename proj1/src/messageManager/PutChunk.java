package messageManager;

import peer.Peer;
import responseManager.SendStored;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PutChunk extends MessageManager {
    private int chunkNo;
    private int replicationDeg;
    private String chunkKey;

    public PutChunk(byte[] data) {
        super(data);
        this.chunkKey = this.fileId + " " + this.chunkNo;
    }

    // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
        this.chunkNo = Integer.parseInt(this.header[4]);
        this.replicationDeg = Integer.parseInt(this.header[5]);
    }

    @Override
    public void run() {
        /*System.out.format("RECEIVED PUTCHUNK version=%s senderId=%s fileId=%s chunkNo=%s replicationDeg=%s \n",
                this.version, this.senderId, this.fileId, this.chunkNo, this.replicationDeg);*/

        if (Peer.id != this.senderId) { // A peer can't send a chunk to itself
            // if(Peer.storage.getChunkOccurrences().get(chunkOccurrencesKey) >= this.replicationDeg)
            //    return;
            String value = this.fileId + " " + this.chunkNo;
            Peer.storage.addRemovedPutChunk(value);

            if (Peer.storage.getChunks().containsKey(chunkKey))
                return;

            if (Peer.storage.getAvailableStorage() - Peer.storage.getTotalStorage() >= body.length) {

                System.out.format("RECEIVED PUTCHUNK version=%s senderId=%s fileId=%s chunkNo=%s replicationDeg=%s \n",
                        this.version, this.senderId, this.fileId, this.chunkNo, this.replicationDeg);
                Peer.scheduledThreadPoolExecutor.schedule(new SendStored(Peer.version, this.fileId, this.chunkNo, this.replicationDeg, this.body),
                        new Random().nextInt(401), TimeUnit.MILLISECONDS);
            }

        }
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }
}
