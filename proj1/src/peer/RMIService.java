package peer;

import java.io.IOException;
import java.rmi.Remote;
import java.security.NoSuchAlgorithmException;

public interface RMIService extends Remote {
    void backup(String path, int replicationDeg) throws IOException, NoSuchAlgorithmException;
}
