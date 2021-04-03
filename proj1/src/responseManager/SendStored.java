package responseManager;

import macros.Macros;
import peer.Peer;

public class SendStored implements Runnable {
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;

    public SendStored(String version, int senderId, String fileId, int chunkNo) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        System.out.format("SENT STORED version=%s senderId=%s fileId=%s chunkNo=%s \n",
                this.version, this.senderId, this.fileId, this.chunkNo);

        String messageStr = this.version + " STORED " + this.senderId + " " + this.fileId + " "
                + this.chunkNo + Macros.CR + Macros.LF + Macros.CR + Macros.LF;

        byte[] message = messageStr.getBytes();

        Peer.mcChannel.send(message);
    }
}
