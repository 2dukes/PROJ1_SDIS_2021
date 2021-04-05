package manageThreads;

import macros.Macros;
import peer.Chunk;
import peer.Peer;
import peer.PeerFile;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RemovedBackup implements Runnable {
    private String fileId;
    private int chunkNo;

    public RemovedBackup(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {

    }
}
