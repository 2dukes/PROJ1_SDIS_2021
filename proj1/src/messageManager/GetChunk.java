package messageManager;

import peer.Chunk;
import peer.Peer;
import responseManager.SendChunk;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetChunk extends MessageManager {
    private int chunkNo;
    public GetChunk(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId) {
            Chunk chunk = Peer.storage.getChunk(this.fileId, this.chunkNo);
            if(chunk == null)
                return;

            Executors.newScheduledThreadPool(150).schedule(new SendChunk(this.version, this.fileId,
                    this.chunkNo, chunk.getData()), new Random().nextInt(401), TimeUnit.MILLISECONDS);
        }
    }
}
