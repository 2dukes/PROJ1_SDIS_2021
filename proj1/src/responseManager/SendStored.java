package responseManager;

public class SendStored implements Runnable {
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;

    public SendStored(String version, String fileId, int chunkNo) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {

    }
}
