package manageThreads;

import peer.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RemovedBackup implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;
    private int desiredReplicationDeg;
    private byte[] data;

    public RemovedBackup(String version, String fileId, int chunkNo, int desiredReplicationDeg, byte[] data) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.desiredReplicationDeg = desiredReplicationDeg;
        this.data = data;
    }

    @Override
    public void run() {
        String value = this.fileId + " " + this.chunkNo;
        if (!Peer.storage.getRemovedPutChunks().contains(value)) {
            String messageStr = "1.0 PUTCHUNK " + Peer.id + " " + this.fileId + " " + this.chunkNo + " " + this.desiredReplicationDeg + "\r\n\r\n";

            byte[] header = messageStr.getBytes();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(header);
                outputStream.write(this.data);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            byte[] message = outputStream.toByteArray();

            System.out.println(messageStr);
            Peer.mdbChannel.send(message);
            Peer.scheduledThreadPoolExecutor.schedule(new manageThreads.PutChunk(message,
                    this.fileId, chunkNo), 1, TimeUnit.SECONDS);
        }
        Peer.storage.deleteRemovedPutChunk(value);
    }
}
