import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PeerStorage implements Serializable {
    public ArrayList<File> files;
    private int availableStorage = (int) (5*Math.pow(10,9)); // ~ 5 GBytes
    private ArrayList<Chunk> chunks;

    public PeerStorage() {
        //this.files = new ArrayList<>();
        /*this.chunks = new ArrayList<>();*/
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public synchronized void updateChunks() {
        for (int i = 0; i < this.files.size(); i++) {
            this.chunks.addAll(this.files.get(i).getChunks());
        }
    }


}
