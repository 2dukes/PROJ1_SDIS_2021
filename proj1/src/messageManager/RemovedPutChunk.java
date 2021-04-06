package messageManager;

import peer.Chunk;
import peer.Peer;
import peer.PeerFile;

import java.util.concurrent.TimeUnit;

public class RemovedPutChunk extends MessageManager {
    private String fileId;
    private String fileIdToSearch;
    private int chunkNo;
    private int chunkNoToSearch;


    public RemovedPutChunk(byte[] data, String fileIdToSearch, int chunkNoToSearch) {
        super(data);
        this.fileIdToSearch = fileIdToSearch;
        this.chunkNoToSearch = chunkNoToSearch;
    }

    @Override
    public void parseSpecificParameters() { this.chunkNo = Integer.parseInt(this.header[4]); }

    @Override
    public void run() {
        if(this.fileId == this.fileIdToSearch && this.chunkNo == this.chunkNoToSearch) {
            // Update HashSet
            String value = this.fileId +  " " + this.chunkNo;
            Peer.storage.addRemovedPutChunk(value);
        }
    }
}
