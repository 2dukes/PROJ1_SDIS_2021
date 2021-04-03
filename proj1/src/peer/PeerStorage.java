package peer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerStorage implements Serializable {
    public ArrayList<PeerFile> peerFiles;
    private int availableStorage = (int) (5 * Math.pow(10, 9)); // ~ 5 GBytes

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private ConcurrentHashMap<String, Chunk> chunks;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.chunks = new ConcurrentHashMap<>();
    }

    public synchronized void addFile(PeerFile peerFile) {
        peerFiles.add(peerFile);
    }

    public synchronized void putChunk(Chunk chunk) {
        String key = chunk.getFileId() + " " + chunk.getChunkNo();
        this.chunks.put(key, chunk);
    }

    public synchronized int getAvailableStorage() {
        return availableStorage;
    }

    public synchronized ArrayList<PeerFile> getPeerFiles() {
        return peerFiles;
    }

    public synchronized Map<String, Chunk> getChunks() {
        return chunks;
    }

    public synchronized void updateAvailableStorage(int chunkSize) {
        this.availableStorage -= chunkSize;
    }

    public synchronized void incrementChunkReplicationDeg(String key) {
        if(this.chunks.containsKey(key)) {
            Chunk chunk = this.chunks.get(key);
            chunk.incrementCurrentReplicationDegree();
            this.chunks.put(key, chunk);
        } else
            updatePeerFileChunkReplicationDeg(key);
    }

    public synchronized void decrementChunkReplicationDeg(String key) {
        if(this.chunks.containsKey(key)) {
            Chunk chunk = this.chunks.get(key);
            chunk.decrementCurrentReplicationDegree();
            this.chunks.put(key, chunk);
        } else
            decreasePeerFileChunkReplicationDeg(key);
    }

    public synchronized PeerFile getPeerFile(String fileId) {
        for(int i = 0; i < this.peerFiles.size(); i++) {
            if(this.peerFiles.get(i).getId().equals(fileId))
                return this.peerFiles.get(i);
        }

        return null;
    }

    public synchronized Chunk getChunkFromPeerFile(PeerFile peerFile, int chunkNo) {
        if(peerFile != null)
            return peerFile.getChunks().get(chunkNo - 1);
        return null;
    }

    public synchronized void updatePeerFileChunkReplicationDeg(String key) {
        String fileId = key.split(" ")[0];
        int chunkNo = Integer.parseInt(key.split(" ")[1]);
        PeerFile peerFile = getPeerFile(fileId);
        Chunk chunk = getChunkFromPeerFile(peerFile, chunkNo);
        if(chunk != null)
            chunk.incrementCurrentReplicationDegree();
        else
            System.out.format("Chunk [fileId=%s | chunkNo=%d] not present in peer.", fileId, chunkNo);
    }

    public synchronized void decreasePeerFileChunkReplicationDeg(String key) {
        String fileId = key.split(" ")[0];
        int chunkNo = Integer.parseInt(key.split(" ")[1]);
        PeerFile peerFile = getPeerFile(fileId);
        Chunk chunk = getChunkFromPeerFile(peerFile, chunkNo);
        if(chunk != null)
            chunk.decrementCurrentReplicationDegree();
        else
            System.out.format("Chunk [fileId=%s | chunkNo=%d] not present in peer.", fileId, chunkNo);
    }


}
