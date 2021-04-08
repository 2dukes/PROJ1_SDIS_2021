package manageThreads;

import macros.Macros;
import peer.Chunk;
import peer.Peer;
import peer.PeerFile;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetChunk implements Runnable {
    private byte[] message;
    private String fileId;
    private int chunkNo;
    private int counter;
    private int time;

    public GetChunk(byte[] message, String fileId, int chunkNo) {
        this.message = message;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.counter = 1;
        this.time = 1;
    }

    @Override
    public void run() {
        if(this.counter++ < 5) {
            Chunk chunk = new Chunk(this.fileId, this.chunkNo, null, 0);
            if(!Peer.storage.getRestoredChunks().contains(chunk)) {
                this.time *= 3;
                Peer.mcChannel.send(message);
                System.out.format("Sent GETCHUNK Retry [%d] for Chunk [fileId=%s | chunkNo=%d]\n", this.counter, this.fileId, this.chunkNo);
                Peer.scheduledThreadPoolExecutor.schedule(this, this.time, TimeUnit.SECONDS);
            }
        }
    }
}
