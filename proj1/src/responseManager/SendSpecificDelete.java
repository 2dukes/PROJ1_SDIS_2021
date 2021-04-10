package responseManager;

import peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SendSpecificDelete implements Runnable {
    private String version;
    private String fileId;
    private int peerId;

    public SendSpecificDelete(String version, String fileId, int peerId) {
        this.version = version;
        this.fileId = fileId;
        this.peerId = peerId;
    }

    @Override
    public void run() {
        // <version> SPECIFIC_DELETE <senderId> <fileId> <peerId> <CRLF><CRLF>
        // Delete Enhancement
        delete();

    }

    public void delete() {
        try {
            String messageStr = this.version + " SPECIFIC_DELETE " + Peer.id + " " + this.fileId + " "
                    + this.peerId + "\r\n\r\n";
            Peer.mcChannel.setDesiredFileId(fileId);

            byte[] header = messageStr.getBytes();

            for (int i = 0; i < 5; i++) {
                if(Peer.storage.getPeersBackingUp().containsKey(this.fileId) && Peer.storage.getPeersBackingUp().get(this.fileId).size() == 0)
                    break;
                else
                    System.out.println(messageStr);

                Peer.mcChannel.send(header);
                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}