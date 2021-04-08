package responseManager;

import messageManager.MessageManager;
import peer.Peer;

public class SendReceivedDelete extends MessageManager {
    private String version;
    private String fileId;
    private int senderId;

    public SendReceivedDelete(String version, int senderId, String fileId) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    @Override
    public void parseSpecificParameters() { }

    @Override
    public void run() {
        // <version> RECEIVED_DELETE <senderId> <fileId> <CRLF><CRLF>
        String messageStr = this.version + " RECEIVED_DELETE " + this.senderId + " " + this.fileId + "\r\n\r\n";
        Peer.mcChannel.send(messageStr.getBytes());
    }
}
