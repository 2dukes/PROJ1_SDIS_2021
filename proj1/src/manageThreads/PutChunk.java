package manageThreads;

import peer.Chunk;
import peer.Peer;
import peer.PeerFile;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PutChunk implements Runnable {
    private byte[] message;
    private String fileId;
    private int chunkNo;
    private int counter;
    private int time;

    public PutChunk(byte[] message, String fileId, int chunkNo) {
        this.message = message;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.counter = 1;
        this.time = 1;
        System.out.println("CONSTRUCTOR!\n");
    }

    @Override
    public void run() {
        PeerFile peerFile = Peer.storage.getPeerFile(this.fileId);
        Chunk chunk = Peer.storage.getChunkFromPeerFile(peerFile, this.chunkNo);

        int currentReplicationDeg = chunk.getCurrentReplicationDegree();
        int desiredReplicationDeg = chunk.getDesiredReplicationDegree();
        System.out.println("Current RepDeg = " + currentReplicationDeg);
        if(currentReplicationDeg < desiredReplicationDeg && this.counter++ < 5) {
            this.time *= 2;
            Peer.mdbChannel.send(message);
            System.out.format("Sent Retry [%d] for Chunk [fileId=%s | chunkNo=%d]\n", this.counter, this.fileId, this.chunkNo);
            Executors.newScheduledThreadPool(150).schedule(this, this.time, TimeUnit.SECONDS);
        }
    }
}
