package messageManager;

import peer.Peer;
import peer.PeerFile;
import responseManager.SendSpecificDelete;

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

            Peer.scheduledThreadPoolExecutor.execute(new SendSpecificDelete(this.version, this.fileId, this.senderId));
        }
    }
}
