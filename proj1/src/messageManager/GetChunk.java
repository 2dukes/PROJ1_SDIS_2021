package messageManager;

import peer.Chunk;
import peer.Peer;
import responseManager.SendChunk;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GetChunk extends MessageManager {
    private int chunkNo;
    private InetAddress IP;

    public GetChunk(byte[] data, InetAddress IP) {
        super(data);
        this.IP = IP;
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId) {
            Chunk chunk = Peer.storage.getChunk(this.fileId, this.chunkNo);
            if (chunk == null)
                return;

            Peer.scheduledThreadPoolExecutor.schedule(new SendChunk(Peer.version, this.fileId,
                    this.chunkNo, chunk.getData(), this.IP), new Random().nextInt(401), TimeUnit.MILLISECONDS);
        }
    }
}
