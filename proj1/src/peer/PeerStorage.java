package peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerStorage implements Serializable {
    public List<PeerFile> peerFiles;
    private long availableStorage = (long) (5 * Math.pow(10, 9)); // ~ 5 GBytes

    // Key: fileId chunkNo
    // Value: Chunk itself
    private ConcurrentHashMap<String, Chunk> chunks;
    private List<Chunk> restoredChunks;

    // Value: fileId chunkNo
    private Set<String> receivedRemovedPutChunks;

    // Key: fileId chunkNo
    // Value: TCP Port
    private ConcurrentHashMap<String, Integer> filePorts;

    // Key: fileId chunkNo
    // Value: Number of times the chunk was stored
    private ConcurrentHashMap<String, Integer> numberOfStoredChunks;

    // Key: fileId
    // Value: Set of Peer Ids backing up the file chunks
    private ConcurrentHashMap<String, Set<Integer>> peersBackingUp;

    // File Id
    private Set<String> filesToRemove;

    private int numberOfReceivedChunks = 0;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.restoredChunks = new ArrayList<>();
        this.chunks = new ConcurrentHashMap<>();
        this.receivedRemovedPutChunks = new HashSet<>();
        this.numberOfStoredChunks = new ConcurrentHashMap<>();
        this.filePorts = new ConcurrentHashMap<>();
        this.peersBackingUp = new ConcurrentHashMap<>();
        this.filesToRemove = new HashSet<>();
    }

    public synchronized void addFile(PeerFile peerFile) {
        for (int i = 0; i < this.peerFiles.size(); i++) {
            if (this.peerFiles.get(i).getId().equals(peerFile.getId())) // Verify if it doesn't exist already
                return;
        }
        peerFiles.add(peerFile);
    }

    public synchronized void addRemovedPutChunk(String value) {
        this.receivedRemovedPutChunks.add(value);
    }

    public synchronized void deleteRemovedPutChunk(String value) {
        this.receivedRemovedPutChunks.remove(value);
    }

    public synchronized Set<String> getRemovedPutChunks() {
        return this.receivedRemovedPutChunks;
    }

    public synchronized void putChunk(Chunk chunk) {
        String key = chunk.getFileId() + " " + chunk.getChunkNo();
        this.chunks.put(key, chunk);
    }

    public synchronized long getAvailableStorage() {
        return this.availableStorage;
    }

    public synchronized List<PeerFile> getPeerFiles() {
        return this.peerFiles;
    }

    public synchronized List<Chunk> getRestoredChunks() {
        return this.restoredChunks;
    }

    public synchronized void deleteRestoredChunks(String fileId, int chunkNo) {
        for (int i = 0; i < this.restoredChunks.size(); i++) {
            if (this.restoredChunks.get(i).getFileId().equals(fileId) && this.restoredChunks.get(i).getChunkNo() == chunkNo) {
                this.restoredChunks.remove(i);
                return;
            }
        }
    }

    public synchronized boolean addRestoredChunk(Chunk chunk) {
        if (!this.restoredChunks.contains(chunk)) {
            this.restoredChunks.add(chunk);
            return true;
        }
        return false;
    }

    public synchronized boolean chunkAlreadyRestored(String fileId, int chunkNo) {
        for (int i = 0; i < this.restoredChunks.size(); i++) {
            if (this.restoredChunks.get(i).getFileId().equals(fileId) && this.restoredChunks.get(i).getChunkNo() == chunkNo)
                return true;
        }
        return false;
    }

    public synchronized Map<String, Chunk> getChunks() {
        return this.chunks;
    }

    public synchronized void setAvailableStorage(long size) {
        this.availableStorage = size;
    }

    public synchronized void incrementChunkReplicationDeg(String key) {
        if (this.chunks.containsKey(key))
            this.chunks.get(key).incrementCurrentReplicationDegree();
        else
            updatePeerFileChunkReplicationDeg(key);
    }

    public synchronized void decrementChunkReplicationDeg(String key) {
        if (this.chunks.containsKey(key)) {
            Chunk chunk = this.chunks.get(key);
            chunk.decrementCurrentReplicationDegree();
            this.chunks.put(key, chunk);
        } else
            decreasePeerFileChunkReplicationDeg(key);
    }

    public synchronized PeerFile getPeerFile(String fileId) {
        for (int i = 0; i < this.peerFiles.size(); i++) {
            if (this.peerFiles.get(i).getId().equals(fileId))
                return this.peerFiles.get(i);
        }

        return null;
    }

    public synchronized Chunk getChunkFromPeerFile(PeerFile peerFile, int chunkNo) {
        if (peerFile != null)
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
        if (chunk != null)
            chunk.incrementCurrentReplicationDegree();
        else
            System.out.format("Chunk [fileId=%s | chunkNo=%d] not present in peer.\n", fileId, chunkNo);
    }

    public synchronized void decreasePeerFileChunkReplicationDeg(String key) {
        String fileId = key.split(" ")[0];
        int chunkNo = Integer.parseInt(key.split(" ")[1]);
        PeerFile peerFile = getPeerFile(fileId);
        Chunk chunk = getChunkFromPeerFile(peerFile, chunkNo);
        if (chunk != null)
            chunk.decrementCurrentReplicationDegree();
        else
            System.out.format("Chunk [fileId=%s | chunkNo=%d] not present in peer.", fileId, chunkNo);
    }

    public synchronized boolean deleteFileChunks(String fileId) {
        try {
            boolean hasDeleted = false;
            for (String key : this.chunks.keySet()) {
                String id = this.chunks.get(key).getFileId();
                if (id.equals(fileId)) {
                    hasDeleted = true;
                    decrementStoredMessage(key);
                    this.chunks.remove(key);
                    deleteChunkFile(key);
                }
            }
            return hasDeleted;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public synchronized void deleteChunkFile(String key) throws Exception {
        File file = new File("../../resources/peers/" + Peer.id + "/chunks/" + key);
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

    public void restoreFile(String filePath, int numberOfExpectedChunks) {
        try {


            String fileId = getFileByPath(filePath).getId();
            int fileRestoredChunksSize = 0;

            for (int i = 0; i < this.restoredChunks.size(); i++) {
                if (this.restoredChunks.get(i).getFileId().equals(fileId)) {
                    System.out.println("Chunk Number " + this.restoredChunks.get(i).getChunkNo());
                    fileRestoredChunksSize++;
                }
            }

            System.out.println("Restored Size " + fileRestoredChunksSize);
            System.out.println("Number of Expected " + numberOfExpectedChunks);

            if (fileRestoredChunksSize != numberOfExpectedChunks)
                throw new Exception("Insufficient number of chunks provided.");

            String[] pathArray = filePath.split("/");
            String path = pathArray[pathArray.length - 1];
            String fileName = "../../resources/peers/" + Peer.id + "/restored/" + path;

            File f = new File(fileName);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            System.out.println("Restoring file...");
            FileOutputStream file = new FileOutputStream(fileName);

            for (int i = 0; i < this.restoredChunks.size(); i++) {
                if (this.restoredChunks.get(i).getFileId().equals(fileId))
                    file.write(this.restoredChunks.get(i).getData(), 0, this.restoredChunks.get(i).getData().length);
            }

            file.close();

            System.out.println("DONE");
            this.restoredChunks.clear();
        } catch (Exception e) {
            System.err.println("Exception was caught: " + e.toString());
        }
    }

    public long getTotalStorage() {
        long total = 0;
        for (String key : this.chunks.keySet()) {
            total += this.chunks.get(key).getData().length;
        }
        return total;
    }

    public void removeChunk(String key) {
        this.chunks.remove(key);
        try {
            deleteChunkFile(key);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public synchronized ConcurrentHashMap<String, Integer> getNumberOfStoredChunks() {
        return this.numberOfStoredChunks;
    }

    public synchronized void incrementStoredMessage(String chunkKey) {
        if (this.numberOfStoredChunks.containsKey(chunkKey))
            this.numberOfStoredChunks.put(chunkKey, this.numberOfStoredChunks.get(chunkKey) + 1);
        else
            this.numberOfStoredChunks.put(chunkKey, 1);
    }

    public synchronized void decrementStoredMessage(String chunkKey) {
        if (this.numberOfStoredChunks.containsKey(chunkKey)) {
            this.numberOfStoredChunks.put(chunkKey, this.numberOfStoredChunks.get(chunkKey) - 1);
            if (this.numberOfStoredChunks.get(chunkKey) == 0)
                this.numberOfStoredChunks.remove(chunkKey);
        }
    }

    public void removeFileByPath(String path) {
        for (PeerFile file : peerFiles)
            if (file.getPath().equals(path)) {
                peerFiles.remove(file);
                return;
            }
    }

    public synchronized ConcurrentHashMap<String, Integer> getFilePorts() {
        return this.filePorts;
    }

    public synchronized int getFilePort(String fileId) throws Exception {
        if (this.filePorts.containsKey(fileId))
            return this.filePorts.get(fileId);
        throw new Exception("Port does not exist for file with id " + fileId);
    }

    public synchronized void addFilePort(String fileId, int port) {
        try {
            if (!this.filePorts.containsKey(fileId))
                this.filePorts.put(fileId, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized ConcurrentHashMap<String, Set<Integer>> getPeersBackingUp() {
        return this.peersBackingUp;
    }

    public synchronized void addPeerBackingUp(String fileId, int peerId) {
        if (!this.peersBackingUp.containsKey(fileId))
            this.peersBackingUp.put(fileId, new HashSet<>(Arrays.asList(peerId)));
        else
            this.peersBackingUp.get(fileId).add(peerId);
    }

    public synchronized boolean removePeerBackingUp(String fileId, int peerId) {
        if (this.peersBackingUp.containsKey(fileId)) {
            this.peersBackingUp.get(fileId).remove(Integer.valueOf(peerId)); // remove by object, not by index
            return true;
        }
        return false;
    }

    private synchronized String getChunkFileIdByKey(String chunkKey) {
        return chunkKey.split(" ")[0];
    }

    public synchronized void decrementStoredMessageByFileId(String fileId) {
        for (String chunkKey : this.numberOfStoredChunks.keySet()) {
            if (getChunkFileIdByKey(chunkKey).equals(fileId)) {
                decrementStoredMessage(chunkKey);
            }
        }
    }

    public synchronized Set<String> getFilesToRemove() {
        return this.filesToRemove;
    }

    public synchronized boolean deleteFileToRemove(String fileId) {
        if (this.filesToRemove.contains(fileId)) {
            this.filesToRemove.remove(fileId); // remove by object, not by index
            return true;
        }
        return false;
    }

    public synchronized void incrementNumberOfReceivedChunks() {
        this.numberOfReceivedChunks++;
    }

    public synchronized void setNumberOfReceivedChunks(int numberOfReceivedChunks) {
        this.numberOfReceivedChunks = numberOfReceivedChunks;
    }

    public synchronized int getNumberOfReceivedChunks() {
        return this.numberOfReceivedChunks;
    }
}
