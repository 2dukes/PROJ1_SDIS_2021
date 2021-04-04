package messageManager;

import peer.Peer;

public class Delete extends MessageManager {
    public Delete(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {}

    @Override
    public void run() {
        if(Peer.id != this.senderId) {
            Peer.storage.deleteFileChunks(this.fileId);

            System.out.format("RECEIVED DELETE version=%s senderId=%s fileId=%s\n",
                    this.version, this.senderId, this.fileId);
        }
    }
}
