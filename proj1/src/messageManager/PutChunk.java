package messageManager;

import peer.*;
import responseManager.SendStored;

import java.io.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PutChunk extends MessageManager {
    private int chunkNo;
    private int replicationDeg;
    String chunkKey;

    public PutChunk(byte[] data) {
        super(data);
        this.chunkKey = this.fileId + " " + this.chunkNo;
    }

    // <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
        this.replicationDeg = Integer.parseInt(this.header[5]);
    }

    @Override
    public synchronized void run() {
        /*System.out.format("RECEIVED PUTCHUNK version=%s senderId=%s fileId=%s chunkNo=%s replicationDeg=%s \n",
                this.version, this.senderId, this.fileId, this.chunkNo, this.replicationDeg);*/

        if(Peer.id != this.senderId) { // A peer can't send a chunk to itself
            // if(Peer.storage.getChunkOccurrences().get(chunkOccurrencesKey) >= this.replicationDeg)
            //    return;
            /*System.out.println("Well...\n");
            System.out.println("Key: " + chunkKey);
            System.out.println("Storage size: " + Peer.storage.getChunks().size());*/
            
            if(Peer.storage.getChunks().containsKey(chunkKey))
                return;
            System.out.println("Passed\n");
            if(Peer.storage.getAvailableStorage() >= body.length) {
                peer.Chunk chunk = new peer.Chunk(this.fileId, this.chunkNo, body, this.replicationDeg);

                Peer.storage.putChunk(chunk);
                Peer.storage.updateAvailableStorage(body.length);

                createChunkFile(chunk.getData());

                System.out.format("RECEIVED PUTCHUNK version=%s senderId=%s fileId=%s chunkNo=%s replicationDeg=%s \n",
                        this.version, this.senderId, this.fileId, this.chunkNo, this.replicationDeg);
                Executors.newScheduledThreadPool(150).schedule(new SendStored(this.version, this.senderId, this.fileId, this.chunkNo),
                        new Random().nextInt(401), TimeUnit.MILLISECONDS);
            }

        }
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }

    public void createChunkFile(byte[] data) {
        try {
            String fileName = "src/files/chunks/" + this.chunkKey + ".txt";

            File f = new File(fileName);
            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            System.out.println("Creating file....");

            FileOutputStream file = new FileOutputStream(fileName);
//          System.out.println(data.length);
            file.write(data, 0, data.length);
            file.close();

            System.out.println("DONE");
        }
        catch(IOException e) {
            System.err.println("Exception was caught: " + e.toString());
        }
    }
}
