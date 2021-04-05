package responseManager;

import peer.Peer;

public class SendStored implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;

    public SendStored(String version, String fileId, int chunkNo) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        System.out.format("SENT STORED version=%s senderId=%s fileId=%s chunkNo=%s \n",
                this.version, Peer.id, this.fileId, this.chunkNo);

        String messageStr = this.version + " STORED " + Peer.id + " " + this.fileId + " "
                + this.chunkNo + "\r\n\r\n";

        byte[] message = messageStr.getBytes();

        Peer.mcChannel.send(message);
    }
}
