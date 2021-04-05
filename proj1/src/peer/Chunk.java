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
        this.currentReplicationDegree = 0;
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

    public void incrementCurrentReplicationDegree() {
        this.currentReplicationDegree++;
    }

    public void decrementCurrentReplicationDegree() {
        this.currentReplicationDegree--;
    }

    public int getCurrentReplicationDegree() {
        return this.currentReplicationDegree;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public static int compareTo(Chunk b, Chunk a) {
        return b.chunkNo - a.chunkNo;
    }

}
