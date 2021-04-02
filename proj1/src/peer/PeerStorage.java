package peer;

import java.io.Serializable;
import java.util.*;

public class PeerStorage implements Serializable {
    public ArrayList<PeerFile> peerFiles;
    private int availableStorage = (int) (5*Math.pow(10,9)); // ~ 5 GBytes

    private ArrayList<Chunk> chunks;

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private Set<String> chunkOccurrences;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.chunkOccurrences = new HashSet<String>();
    }

    public void addFile(PeerFile peerFile) {
        this.peerFiles.add(peerFile);
    }

    public synchronized void updateChunks() {
        for (int i = 0; i < this.peerFiles.size(); i++) {
            this.chunks.addAll(this.peerFiles.get(i).getChunks());
        }
    }

    public int getAvailableStorage() {
        return availableStorage;
    }

    public Set<String> getChunkOccurrences() {
        return chunkOccurrences;
    }

    public ArrayList<PeerFile> getPeerFiles() {
        return peerFiles;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }
}
