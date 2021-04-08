package messageManager;

import peer.Peer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedDelete extends MessageManager {
    private String desiredFileId;

    public ReceivedDelete(byte[] data, String desiredFileId) {
        super(data);
        this.desiredFileId = desiredFileId;
    }

    @Override
    public void parseSpecificParameters() {

    }

    @Override
    public void run() {
        if (Peer.id != this.senderId && this.version.equals("2.0") && this.desiredFileId != null && this.desiredFileId.equals(this.fileId) && Peer.isInitiator) {
            Peer.storage.removePeerBackingUp(this.fileId, this.senderId);

            System.out.format("RECEIVED RECEIVED_DELETE version=%s senderId=%s fileId=%s\n",
                    this.version, this.senderId, this.fileId);
        }

    }
}
