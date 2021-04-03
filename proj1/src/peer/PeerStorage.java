package peer;

import java.io.Serializable;
import java.util.*;

public class PeerStorage implements Serializable {
    public ArrayList<PeerFile> peerFiles;
    private int availableStorage = (int) (5*Math.pow(10,9)); // ~ 5 GBytes

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private Map<String, Chunk> chunks;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.chunks = new HashMap<>();
    }

    public List<Chunk> addFile(PeerFile peerFile) {
        List<Chunk> fileChunks = peerFile.getChunks();
        for (int i = 0; i < fileChunks.size(); i++) {
            String key = fileChunks.get(i).getFileId() + " " + fileChunks.get(i).getChunkNo();
            this.chunks.put(key, fileChunks.get(i));
        }
        this.peerFiles.add(peerFile);
        return fileChunks;
    }

    public void putChunk(Chunk chunk) {
        String key = chunk.getFileId() + " " + chunk.getChunkNo();
        this.chunks.put(key, chunk);
    }

    public int getAvailableStorage() {
        return availableStorage;
    }

    public ArrayList<PeerFile> getPeerFiles() {
        return peerFiles;
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public void updateAvailableStorage(int chunkSize) {
        this.availableStorage -= chunkSize;
    }
}
