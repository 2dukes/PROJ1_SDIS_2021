package peer;

import java.io.Serializable;

public class Chunk implements Serializable {
    private String fileId;
    private int chunkNo;
    private byte[] data;
    private int desiredReplicationDegree;
    private int currentReplicationDegree;

    public Chunk(String fileId, int chunkNo, byte[] data, int desiredReplicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.data = data;
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public String getFileId() {
        return this.fileId;
    }

    public int getChunkNo() {
        return this.chunkNo;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getCurrentReplicationDegree() {
        return this.currentReplicationDegree;
    }

    public void setCurrentReplicationDegree(int replicationDegree) {
        this.currentReplicationDegree = replicationDegree;
    }

}
