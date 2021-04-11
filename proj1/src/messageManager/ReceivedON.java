package messageManager;

import peer.Peer;
import responseManager.SendSpecificDelete;

public class ReceivedON extends MessageManager {

    public ReceivedON(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId && this.version.equals("2.0")) {
            System.out.format("RECEIVED ON version=%s senderId=%s\n",
                    this.version, this.senderId);

            for (String key : Peer.storage.getPeersBackingUp().keySet()) {
                if (Peer.storage.getPeersBackingUp().get(key).contains(this.senderId) && Peer.storage.getFilesToRemove().contains(key))
                        Peer.scheduledThreadPoolExecutor.execute(new SendSpecificDelete(this.version, key, this.senderId));
            }
        }
    }
}
