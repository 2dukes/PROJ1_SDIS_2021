package peer;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface RMIService extends Remote {
    void backup(String path, int replicationDeg) throws IOException, NoSuchAlgorithmException, RemoteException, InterruptedException;
    void delete(String path) throws RemoteException;
    void restore(String path) throws Exception;
    void reclaim(int maximumDiskSpace) throws RemoteException;
    void setInitiator(boolean isInitiator) throws RemoteException;
    String state() throws RemoteException;
}
