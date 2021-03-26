import java.io.Serializable;
import java.util.ArrayList;

public class PeerStorage implements Serializable {
    public ArrayList<PeerFile> peerFiles;
    private int availableStorage = (int) (5*Math.pow(10,9)); // ~ 5 GBytes
    private ArrayList<Chunk> chunks;

    public PeerStorage() {
        this.peerFiles = new ArrayList<>();
        this.chunks = new ArrayList<>();
    }

    public void addFile(PeerFile peerFile) {
        this.peerFiles.add(peerFile);
    }

    public synchronized void updateChunks() {
        for (int i = 0; i < this.peerFiles.size(); i++) {
            this.chunks.addAll(this.peerFiles.get(i).getChunks());
        }
    }


}
