package messageManager;

import peer.Peer;

public class ReceivedDelete extends MessageManager {
    private String desiredFileId;

    public ReceivedDelete(byte[] data, String desiredFileId) {
        super(data);
        this.desiredFileId = desiredFileId;
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId && Peer.version.equals("2.0")) {
            if(Peer.storage.removePeerBackingUp(this.fileId, this.senderId)) {
                if(Peer.storage.getPeersBackingUp().contains(this.fileId) && Peer.storage.getPeersBackingUp().get(this.fileId).size() == 0)
                    Peer.storage.deleteFileToRemove(this.fileId);
                System.out.format("RECEIVED RECEIVED_DELETE version=%s senderId=%s fileId=%s\n",
                        this.version, this.senderId, this.fileId);
            }
            Peer.storage.decrementStoredMessageByFileId(this.fileId);
        }

    }
}
