package messageManager;

import peer.Peer;
import responseManager.SendReceivedDelete;

public class Delete extends MessageManager {
    public Delete(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {}

    @Override
    public void run() {
        if(Peer.id != this.senderId) {
            if (Peer.storage.deleteFileChunks(this.fileId)) {

                System.out.format("RECEIVED DELETE version=%s senderId=%s fileId=%s\n",
                        this.version, this.senderId, this.fileId);

                // Send Delete Response - Delete Enhancement
                // <version> RECEIVED_DELETE <senderId> <fileId> <CRLF><CRLF>
                Peer.scheduledThreadPoolExecutor.execute(new SendReceivedDelete(this.version, Peer.id, this.fileId));
                System.out.format("SENT RECEIVED_DELETE version=%s senderId=%s fileId=%s\n",
                        this.version, this.senderId, this.fileId);
            }
        }
    }
}
