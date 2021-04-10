package messageManager;

import peer.Peer;
import responseManager.SendReceivedDelete;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedSpecificDelete extends MessageManager {
    private int peerId;

    public ReceivedSpecificDelete(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
        this.peerId = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        // <version> SPECIFIC_DELETE <senderId> <fileId> <peerId> <CRLF><CRLF>
        if (Peer.id == this.peerId && this.version.equals("2.0")) {
            if (Peer.storage.deleteFileChunks(this.fileId)) {
                System.out.format("RECEIVED SPECIFIC_DELETE version=%s senderId=%s fileId=%s peerId=%d\n",
                        this.version, this.senderId, this.fileId, this.peerId);

                // Send Delete Response - Delete Enhancement
                // <version> RECEIVED_DELETE <senderId> <fileId> <CRLF><CRLF>
                Peer.scheduledThreadPoolExecutor.execute(new SendReceivedDelete(this.version, Peer.id, this.fileId));
                System.out.format("SENT RECEIVED_DELETE version=%s senderId=%s fileId=%s\n",
                        this.version, this.senderId, this.fileId);
            }
        }

    }
}
