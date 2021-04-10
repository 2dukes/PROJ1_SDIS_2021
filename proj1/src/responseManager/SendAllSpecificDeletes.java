package responseManager;

import peer.Peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SendAllSpecificDeletes implements Runnable {
    private String version;

    public SendAllSpecificDeletes(String version) {
        this.version = version;
    }

    @Override
    public void run() {
        // <version> SPECIFIC_DELETE <senderId> <fileId> <peerId> <CRLF><CRLF>
        // Delete Enhancement
         for (String key : Peer.storage.getPeersBackingUp().keySet())
             for (int peerId : Peer.storage.getPeersBackingUp().get(key))
                Peer.scheduledThreadPoolExecutor.execute(new SendSpecificDelete(this.version, key, peerId));
    }
}