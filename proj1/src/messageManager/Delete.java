package messageManager;

import peer.Peer;
import responseManager.SendReceivedDelete;

public class Delete extends MessageManager {
    public Delete(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId) {
            if (Peer.storage.deleteFileChunks(this.fileId)) {

                System.out.format("RECEIVED DELETE version=%s senderId=%s fileId=%s\n",
                        this.version, this.senderId, this.fileId);

                if (this.version.equals("2.0")) {
                    // Send Delete Response - Delete Enhancement
                    // <version> RECEIVED_DELETE <senderId> <fileId> <CRLF><CRLF>
                    Peer.scheduledThreadPoolExecutor.execute(new SendReceivedDelete(this.version, Peer.id, this.fileId));
                    System.out.format("SENT RECEIVED_DELETE version=%s senderId=%s fileId=%s\n",
                            this.version, Peer.id, this.fileId);
                }


            }
        }
    }
}
