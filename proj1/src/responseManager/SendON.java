package responseManager;

import peer.Peer;

public class SendON implements Runnable {
    private String version;
    private int senderId;

    public SendON(String version, int senderId) {
        this.version = version;
        this.senderId = senderId;
    }

    @Override
    public void run() {
        // <version> ON <senderId> <CRLF><CRLF> - Restore/Delete Enhancement

        String messageStr = this.version + " ON " + this.senderId + "\r\n\r\n";
        Peer.mcChannel.send(messageStr.getBytes());

    }
}
