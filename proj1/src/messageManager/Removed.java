package messageManager;

import macros.Macros;
import peer.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

            peer.Chunk chunk = Peer.storage.getChunk(this.fileId, this.chunkNo);

            if (chunk.getCurrentReplicationDegree() < chunk.getDesiredReplicationDegree()) {
                Peer.scheduledThreadPoolExecutor.schedule(new manageThreads.RemovedBackup(this.fileId,
                        chunkNo), new Random().nextInt(401), TimeUnit.MILLISECONDS);

                String messageStr = "1.0 PUTCHUNK " + Peer.id + " " + this.fileId + " " + chunkNo + " " + chunk.getDesiredReplicationDegree() + "\r\n\r\n"; // HardCoded ID

                byte[] header = messageStr.getBytes();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    outputStream.write(header);
                    outputStream.write(chunk.getData());
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                byte[] message = outputStream.toByteArray();

                System.out.println(messageStr);
                Peer.mdbChannel.send(message);
                Peer.scheduledThreadPoolExecutor.schedule(new manageThreads.PutChunk(message,
                        this.fileId, chunkNo), 1, TimeUnit.SECONDS);

            }

            System.out.format("RECEIVED REMOVED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                    this.version, this.senderId, this.fileId, this.chunkNo);

        }
    }
}
