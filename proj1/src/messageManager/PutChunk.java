package messageManager;

import peer.*;
import responseManager.SendStored;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PutChunk extends MessageManager {
    private int chunkNo;
    private int replicationDeg;

    public PutChunk(byte[] data) {
        super(data);
    }

    // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
        this.replicationDeg = Integer.parseInt(this.header[5]);
    }

    @Override
    public synchronized void run() {
        String chunkOccurrencesKey = this.fileId + " " + this.chunkNo;

        if(!Peer.storage.getChunkOccurrences().contains(chunkOccurrencesKey)) { // Insert new chunk occurrence
            Peer.storage.getChunkOccurrences().add(chunkOccurrencesKey);
        }

        if(Peer.id != this.senderId) { // A peer can't send a chunk to itself
            // if(Peer.storage.getChunkOccurrences().get(chunkOccurrencesKey) >= this.replicationDeg)
            //    return;

            if(Peer.storage.getAvailableStorage() >= body.length) {
                peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, body, this.replicationDeg);

            }

            System.out.format("RECEIVED PUTCHUNK version=%s senderId=%s fileId=%s chunkNo=%s replicationDeg=%s \n",
                    this.version, this.senderId, this.fileId, this.chunkNo, this.replicationDeg);
            Executors.newScheduledThreadPool(150).schedule(new SendStored(this.version, this.fileId, this.chunkNo),
                    new Random().nextInt(401), TimeUnit.MILLISECONDS);
        }


    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }
}
