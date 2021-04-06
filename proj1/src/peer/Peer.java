package peer;

import channels.MCChannel;
import channels.MDBChannel;
import channels.MDRChannel;
import macros.Macros;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements RMIService {
    public static int id;
    public static boolean isInitiator;
    public static String version;
    public static String accessPoint;
    public static MCChannel mcChannel;
    public static MDBChannel mdbChannel;
    public static MDRChannel mdrChannel;
    public static PeerStorage storage;
    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public Peer(String IP_MC, int PORT_MC, String IP_MDB, int PORT_MDB, String IP_MDR, int PORT_MDR) throws SocketException, UnknownHostException {
        mcChannel = new MCChannel(IP_MC, PORT_MC);
        mdbChannel = new MDBChannel(IP_MDB, PORT_MDB);
        mdrChannel = new MDRChannel(IP_MDR, PORT_MDR);
        scheduledThreadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(Macros.NUM_THREADS);
        isInitiator = false;
    }

    public static void main(String[] args) {
        if (args.length != 9) {
            System.err.println("Usage: java Peer <version> <id> <access point> <IP-MC> <Port-MC> <IP-MDB> <Port-MDB> <IP-MDR> <Port-MDR>");
            return;
        }
        try {
            version = args[0];
            id = Integer.parseInt(args[1]);
            accessPoint = args[2];

            Remote obj = new Peer(args[3], Integer.parseInt(args[4]), args[5], Integer.parseInt(args[6]), args[7], Integer.parseInt(args[8]));
            RMIService stub = (RMIService) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);

        } catch (Exception e) {
            System.err.println("Bad arguments usage: " + e.getMessage());
        }

        storage = new PeerStorage();

//        PeerFile peerFile = new PeerFile("src/files/hello.txt", 2, id);
//        storage.peerFiles.add(peerFile);

        //deserializeStorage();

        // https://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
        //Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage));
        // deserializeStorage();


        scheduledThreadPoolExecutor.execute(mdbChannel);
        scheduledThreadPoolExecutor.execute(mcChannel);
        scheduledThreadPoolExecutor.execute(mdrChannel);
        System.out.print("Hello :)");
        System.out.println("My id: " + id);
    }

    public static void serializeStorage() {
        try {
            String fileName = "src/files/" + Peer.id + "/peerStorage.ser";

            File f = new File(fileName);
            if(!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            FileOutputStream file = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(storage);
            out.close();
            file.close();
            Peer.isInitiator = false;
        }
        catch(IOException e) {
            System.err.println("Exception was caught: " + e.toString());
        }
    }

    public static void deserializeStorage() {
        try  {
            String fileName = "src/files/" + Peer.id + "/peerStorage.ser";
            File f = new File(fileName);
            if(!f.exists())
                storage = new PeerStorage();
            else {
                FileInputStream file = new FileInputStream(fileName);
                ObjectInputStream in = new ObjectInputStream(file);
                storage = (PeerStorage) in.readObject();
                in.close();
                file.close();
            }
        }
        catch(Exception e) {
            System.err.println("Exception was caught: " + e.getMessage());
        }
    }

    public void backup(String path, int replicationDeg) throws IOException, NoSuchAlgorithmException, RemoteException {

        PeerFile peerFile = new PeerFile(path, replicationDeg, Peer.id);
        List<Chunk> fileChunks = peerFile.getChunks();
        Peer.storage.addFile(peerFile);

        for(int i = 0; i < fileChunks.size(); i++) {
            Chunk chunk = fileChunks.get(i);
            int chunkNo = i + 1;
            String messageStr = "1.0 PUTCHUNK " + id + " " + peerFile.getId() + " " + chunkNo + " " + replicationDeg + "\r\n\r\n"; // HardCoded ID

            byte[] header = messageStr.getBytes();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(chunk.getData());

            byte[] message = outputStream.toByteArray();

            System.out.println(messageStr);
            mdbChannel.send(message);
            scheduledThreadPoolExecutor.schedule(new manageThreads.PutChunk(message,
                    peerFile.getId(), chunkNo), 1, TimeUnit.SECONDS);
        }
    }

    public void delete(String path) throws RemoteException {
        try {
            PeerFile peerFile = storage.getFileByPath(path);
            String messageStr = "1.0 DELETE " + id + " " + peerFile.getId() + "\r\n\r\n";

            byte[] header = messageStr.getBytes();

            for (int i = 0; i < 5; i++)
                mcChannel.send(header);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void restore(String path) throws Exception {
        PeerFile peerFile = storage.getFileByPath(path);
        int fileChunksSize = peerFile.getChunks().size();

        for(int i = 0; i < fileChunksSize; i++) {
            int chunkNo = i + 1;
            String messageStr = "1.0 GETCHUNK " + id + " " + peerFile.getId() + " " + chunkNo + "\r\n\r\n";

            byte[] message = messageStr.getBytes();

            System.out.println(messageStr);
            mcChannel.send(message);
            scheduledThreadPoolExecutor.schedule(new manageThreads.GetChunk(message,
                    peerFile.getId(), chunkNo), 1, TimeUnit.SECONDS);
        }

        Thread.sleep(fileChunksSize * 20);

        // Sort receivedChunks
        storage.getRestoredChunks().sort(Chunk::compareTo);

        // Create File
        storage.restoreFile(path, peerFile.getChunks().size());
    }

    public void reclaim(int maximumDiskSpace) throws RemoteException {
        int totalStorage = storage.getTotalStorage();
        int toRemove = totalStorage - maximumDiskSpace;

        if (toRemove < 0)
            storage.increaseAvailableStorage(-toRemove);
        else if (toRemove > 0) {
            List<Chunk> chunksList = new ArrayList<>(storage.getChunks().values());
            chunksList.sort((c1, c2) -> Chunk.compareToReplicationDeg(c2, c1));
            int i = 0, removed = 0;
            while (removed < toRemove && i < chunksList.size()) {
                Chunk chunk = chunksList.get(i);
                storage.removeChunk(chunk.getKey());
                removed += chunk.getData().length;

                String messageStr = "1.0 REMOVED " + id + " " + chunk.getFileId() + " " + chunk.getChunkNo() + "\r\n\r\n";

                byte[] message = messageStr.getBytes();

                System.out.println(messageStr);
                mcChannel.send(message);
                i++;
            }
            storage.decreaseAvailableStorage(removed);
        }
    }

    public void setInitiator(boolean isInitiator) {
        Peer.isInitiator = isInitiator;
    }
}
