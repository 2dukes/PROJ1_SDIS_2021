package peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerStorage implements Serializable {
    public List<PeerFile> peerFiles;
    private int availableStorage = (int) (5 * Math.pow(10, 9)); // ~ 5 GBytes

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private ConcurrentHashMap<String, Chunk> chunks;
    private List<Chunk> restoredChunks;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.restoredChunks = new ArrayList<>();
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
        return this.availableStorage;
    }

    public synchronized List<PeerFile> getPeerFiles() {
        return this.peerFiles;
    }

    public synchronized List<Chunk> getRestoredChunks() { return this.restoredChunks; }

    public synchronized void deleteRestoredChunks(String fileId, int chunkNo) {
        for (int i = 0; i < this.restoredChunks.size(); i++) {
            if(this.restoredChunks.get(i).getFileId().equals(fileId) && this.restoredChunks.get(i).getChunkNo() == chunkNo) {
                this.restoredChunks.remove(i);
                return;
            }
        }
    }

    public synchronized void addRestoredChunk(Chunk chunk) {
        this.restoredChunks.add(chunk);
    }

    public synchronized boolean chunkAlreadyRestored(String fileId, int chunkNo) {
        for (int i = 0; i < this.restoredChunks.size(); i++) {
            if(this.restoredChunks.get(i).getFileId().equals(fileId) && this.restoredChunks.get(i).getChunkNo() == chunkNo)
                return true;
        }
        return false;
    }

    public synchronized Map<String, Chunk> getChunks() {
        return this.chunks;
    }

    public synchronized void decreaseAvailableStorage(int chunkSize) {
        this.availableStorage -= chunkSize;
    }

    public synchronized void increaseAvailableStorage(int chunkSize) {
        this.availableStorage += chunkSize;
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

    public synchronized Chunk getChunk(String fileId, int chunkNo) {
        for (String key : this.chunks.keySet()) {
            if (this.chunks.get(key).getFileId().equals(fileId) && this.chunks.get(key).getChunkNo() == chunkNo)
                return this.chunks.get(key);
        }
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

    public void deleteFileChunks(String fileId) {
        try {
            for (String key : this.chunks.keySet()) {
                String id = this.chunks.get(key).getFileId();
                if (id.equals(fileId)) {
                    this.chunks.remove(id);
                    deleteChunkFile(key);
                    increaseAvailableStorage(this.chunks.get(key).getData().length);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteChunkFile(String key) throws Exception {
        File file = new File("src/files/chunks/" + Peer.id + "/" + key + ".txt");
        if (!file.delete()) {
            throw new Exception("Chunk file with key = " + key + " does not exist or was already deleted.");
        }
    }

    public PeerFile getFileByPath(String path) throws Exception {
        for (int i = 0; i < this.peerFiles.size(); i++) {
            if (this.peerFiles.get(i).getPath().equals(path))
                return this.peerFiles.get(i);
        }
        throw new Exception("Could not find the file with path " + path);
    }

    public void restoreFile(String filePath) {
        try {
            String[] pathArray = filePath.split("/");
            String path = pathArray[filePath.length() - 1];
            String fileName = "src/files/restored/" + Peer.id + "/" + path;

            File f = new File(fileName);
            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            System.out.println("Restoring file....");
            FileOutputStream file = new FileOutputStream(fileName);

            for(int i = 0; i < this.restoredChunks.size(); i++)
                file.write(this.restoredChunks.get(i).getData(), 0, this.restoredChunks.get(i).getData().length);

            file.close();

            System.out.println("DONE");
        }
        catch(IOException e) {
            System.err.println("Exception was caught: " + e.toString());
        }
    }
}
