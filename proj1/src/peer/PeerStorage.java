package peer;

import java.io.Serializable;
import java.util.*;

public class PeerStorage implements Serializable {
    public ArrayList<PeerFile> peerFiles;
    private int availableStorage = (int) (5*Math.pow(10,9)); // ~ 5 GBytes

    private ArrayList<Chunk> chunks;

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private Set<String> chunksStored;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.chunksStored = new HashSet<>();
    }

    public void addFile(PeerFile peerFile) {
        this.chunks.addAll(peerFile.getChunks());
        this.peerFiles.add(peerFile);
    }

    public void putChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public int getAvailableStorage() {
        return availableStorage;
    }

    public Set<String> getChunksStored() {
        return chunksStored;
    }

    public ArrayList<PeerFile> getPeerFiles() {
        return peerFiles;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void updateAvailableStorage(int chunkSize) {
        this.availableStorage -= chunkSize;
    }
}
