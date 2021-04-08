package messageManager;

import peer.Peer;
import peer.PeerFile;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedON extends MessageManager {

    public ReceivedON(byte[] data) {
        super(data);
    }

    @Override
    public void parseSpecificParameters() { }

    @Override
    public void run() {
        if (Peer.id != this.senderId && this.version.equals("2.0") && Peer.isInitiator) {
            System.out.format("RECEIVED ON version=%s senderId=%s\n",
                    this.version, this.senderId);
            for (String key : Peer.storage.getPeersBackingUp().keySet()) {
                if (Peer.storage.getPeersBackingUp().get(key).contains(this.senderId))
                    delete(key);
            }
        }
    }

    public void delete(String fileId) {
        try {
            String messageStr = this.version + " DELETE " + Peer.id + " " + fileId + "\r\n\r\n";
            Peer.mcChannel.setDesiredFileId(fileId);

            System.out.println(messageStr);

            byte[] header = messageStr.getBytes();

            for (int i = 0; i < 5; i++) {
                if(this.version.equals("2.0") && Peer.storage.getPeersBackingUp().get(fileId).size() == 0)
                    break;

                Peer.mcChannel.send(header);
                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
