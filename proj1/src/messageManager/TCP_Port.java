package messageManager;

import peer.Peer;

public class TCP_Port extends MessageManager {
    private int port;

    public TCP_Port(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() {
        this.fileId = this.header[3];
        this.port = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        if (Peer.id != this.senderId && this.version.equals("2.0")) {

            Peer.storage.addFilePort(this.fileId, this.port);

            System.out.format("RECEIVED TCP_PORT version=%s senderId=%s fileId=%s portNumber=%s\n",
                    this.version, this.senderId, this.fileId, this.port);
        }
    }
}
