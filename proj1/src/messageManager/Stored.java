package messageManager;

public class Stored extends MessageManager {
    private int chunkNo;

    public Stored(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {


        System.out.format("RECEIVED STORED version=%s senderId=%s fileId=%s chunkNo=%s\n",
                this.version, this.senderId, this.fileId, this.chunkNo);

    }
}
