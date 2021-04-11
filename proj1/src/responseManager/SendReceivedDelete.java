package responseManager;

import peer.Peer;

public class SendReceivedDelete implements Runnable {
    private String version;
    private String fileId;
    private int senderId;

    public SendReceivedDelete(String version, int senderId, String fileId) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }


    @Override
    public void run() {
        Peer.storage.decrementStoredMessageByFileId(this.fileId);

        // <version> RECEIVED_DELETE <senderId> <fileId> <CRLF><CRLF>
        String messageStr = Peer.version + " RECEIVED_DELETE " + this.senderId + " " + this.fileId + "\r\n\r\n";
        Peer.mcChannel.send(messageStr.getBytes());
    }
}
