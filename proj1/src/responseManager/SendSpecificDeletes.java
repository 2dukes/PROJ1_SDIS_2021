package responseManager;

import peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SendSpecificDeletes implements Runnable {
    private String version;

    public SendSpecificDeletes(String version) {
        this.version = version;
    }

    @Override
    public void run() {
        // <version> SPECIFIC_DELETE <senderId> <fileId> <peerId> <CRLF><CRLF>
        // Delete Enhancement
        for (String key : Peer.storage.getPeersBackingUp().keySet())
            for (int peerId : Peer.storage.getPeersBackingUp().get(key))
                delete(key, peerId);

    }

    public void delete(String fileId, int peerId) {
        try {
            String messageStr = this.version + " SPECIFIC_DELETE " + Peer.id + " " + fileId + " "
                    + peerId + "\r\n\r\n";
            Peer.mcChannel.setDesiredFileId(fileId);

            byte[] header = messageStr.getBytes();

            for (int i = 0; i < 5; i++) {
                if(Peer.storage.getPeersBackingUp().containsKey(fileId) && Peer.storage.getPeersBackingUp().get(fileId).size() == 0)
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