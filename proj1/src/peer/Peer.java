package peer;

import Exceptions.FileModifiedException;
import channels.MCChannel;
import channels.MDBChannel;
import channels.MDRChannel;
import macros.Macros;
import responseManager.SendTCPPorts;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
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
import java.util.concurrent.Semaphore;
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
    public static Semaphore semaphore;

    public Peer(String IP_MC, int PORT_MC, String IP_MDB, int PORT_MDB, String IP_MDR, int PORT_MDR) throws SocketException, UnknownHostException {
        mcChannel = new MCChannel(IP_MC, PORT_MC);
        mdbChannel = new MDBChannel(IP_MDB, PORT_MDB);
        mdrChannel = new MDRChannel(IP_MDR, PORT_MDR);
        scheduledThreadPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(Macros.NUM_THREADS);
        isInitiator = false;
        semaphore = new Semaphore(1, true);
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
        System.out.print("Hello :) ");
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

            isInitiator = false;
            storage.getRemovedPutChunks().clear();
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

    public void backup(String path, int replicationDeg) throws IOException, NoSuchAlgorithmException, RemoteException, InterruptedException {
        PeerFile peerFile = null;

        try {
            peerFile = new PeerFile(path, replicationDeg, Peer.id);
            String fileWithPathId = peerFile.getId();
            for (int i = 0; i < storage.getPeerFiles().size(); i++) {
                if(storage.getPeerFiles().get(i).getPath().equals(path)) {
                    if(!storage.getPeerFiles().get(i).getId().equals(fileWithPathId))
                        throw new FileModifiedException("The file you're looking for was modified. Initiating (old) chunks delete protocol...");
                }
            }
        } catch (IOException e) { // File doesn't exist or was deleted
            System.out.println("The file you're looking for doesn't exist!");
            for (int i = 0; i < storage.getPeerFiles().size(); i++) {
                if (storage.getPeerFiles().get(i).getPath().equals(path)) {
                    System.out.println("Deleting the chunks of the file with path " + path);
                    delete(path);
                    return;
                }
            }
            return;
        } catch (FileModifiedException e){
            System.out.println(e.getMessage());
            delete(path);
        }

        List<Chunk> fileChunks = peerFile.getChunks();
        Peer.storage.addFile(peerFile);

        for(int i = 0; i < fileChunks.size(); i++) {
            Chunk chunk = fileChunks.get(i);
            int chunkNo = i + 1;
            String messageStr = this.version + " PUTCHUNK " + id + " " + peerFile.getId() + " " + chunkNo + " " + replicationDeg + "\r\n\r\n"; // HardCoded ID

            byte[] header = messageStr.getBytes();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(chunk.getData());

            byte[] message = outputStream.toByteArray();

            System.out.println(messageStr);
            mdbChannel.send(message);
            Thread.sleep(15);
            scheduledThreadPoolExecutor.schedule(new manageThreads.PutChunk(message,
                    peerFile.getId(), chunkNo), 1, TimeUnit.SECONDS);

        }
    }

    public void delete(String path) throws RemoteException {
        try {

            PeerFile peerFile = storage.getFileByPath(path);
            String messageStr = this.version + " DELETE " + id + " " + peerFile.getId() + "\r\n\r\n";

            byte[] header = messageStr.getBytes();

            for (int i = 0; i < 5; i++) {
                mcChannel.send(header);
                Thread.sleep(10);
            }

            storage.removeFileByPath(path);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void restore(String path) throws Exception {
        PeerFile peerFile = storage.getFileByPath(path);
        int fileChunksSize = peerFile.getChunks().size();
        String desiredFileId = peerFile.getId();
        mdrChannel.setDesiredFileId(desiredFileId);

        if(version.equals("2.0")) { // Restore Enhancement
            int port = SendTCPPorts.findAvailablePort();
            Peer.storage.addFilePort(desiredFileId, port);
            scheduledThreadPoolExecutor.execute(new SendTCPPorts(version, desiredFileId, port));
            Thread.sleep(1000);
            scheduledThreadPoolExecutor.execute(new messageManager.ReceiveChunkTCP(desiredFileId));
        }

        for(int i = 0; i < fileChunksSize; i++) {
            int chunkNo = i + 1;
            String messageStr = this.version + " GETCHUNK " + id + " " + peerFile.getId() + " " + chunkNo + "\r\n\r\n";

            byte[] message = messageStr.getBytes();

            System.out.println(messageStr);
            mcChannel.send(message);
            scheduledThreadPoolExecutor.schedule(new manageThreads.GetChunk(message,
                    peerFile.getId(), chunkNo), 1, TimeUnit.SECONDS);
        }
        if(this.version.equals("2.0"))
            Thread.sleep(1000 + fileChunksSize * 60);
        else {
            if(fileChunksSize > 150)
                Thread.sleep(1000 + fileChunksSize * 60 + 20000);
            Thread.sleep(1000 + fileChunksSize * 60);
        }

        // Sort receivedChunks
        storage.getRestoredChunks().sort(Chunk::compareTo);

        // Create File
        storage.restoreFile(path, peerFile.getChunks().size());
    }

    public void reclaim(int diskSpace) throws RemoteException {
        int maximumDiskSpace = diskSpace * 1000; // Convert to Bytes
        int totalStorage = storage.getTotalStorage();
        int toRemove = totalStorage - maximumDiskSpace;

        storage.setAvailableStorage(maximumDiskSpace);

        if (toRemove > 0) {
            List<Chunk> chunksList = new ArrayList<>(storage.getChunks().values());
            chunksList.sort((c1, c2) -> Chunk.compareToReplicationDeg(c2, c1));
            int i = 0, removed = 0;

            while (removed < toRemove && i < chunksList.size()) {
                Chunk chunk = chunksList.get(i);
                storage.removeChunk(chunk.getKey());
                removed += chunk.getData().length;

                Peer.storage.decrementStoredMessage(chunk.getKey());
                String messageStr = this.version + " REMOVED " + id + " " + chunk.getFileId() + " " + chunk.getChunkNo() + "\r\n\r\n";

                byte[] message = messageStr.getBytes();

                System.out.println(messageStr);
                mcChannel.send(message);
                i++;
            }
        }
    }

    public String state() throws RemoteException {
        String peerState = "\n\n\n------------------------------- Files Backed Up: -------------------------------\n\n\n";

        for(int i = 0; i < storage.getPeerFiles().size(); i++) {
            PeerFile peerFile = storage.getPeerFiles().get(i);
            peerState += "\n\nFilename: " + peerFile.getPath() + "\n";
            peerState += "Backup ID: " + peerFile.getId() + "\n";
            peerState += "Desired Replication Degree: " + peerFile.getReplicationDegree() + "\n";
            peerState += "[Chunks]\n\n";
            for(int j = 0; j < peerFile.getChunks().size(); j++) {
                Chunk chunk = peerFile.getChunks().get(j);
                peerState += "\tChunk [id=" + chunk.getChunkNo() + " | perceivedReplicationDeg=" + chunk.getCurrentReplicationDegree() + "]\n";
            }
        }

        peerState += "\n\n\n------------------------------- Chunks Stored: -------------------------------\n\n\n";
        for (String key : storage.getChunks().keySet()) {
            Chunk chunk = storage.getChunks().get(key);
            peerState += "\n\nChunk ID: " + chunk.getChunkNo() + "\n";
            peerState += "Chunk Size: " + (double) chunk.getData().length / 1000 + " kB\n";
            peerState += "Chunk Desired Replication Degree: " + chunk.getDesiredReplicationDegree() + "\n";
            peerState += "Chunk Perceived Replication Degree: " + chunk.getCurrentReplicationDegree() + "\n";
        }

        peerState += "\n\n\n------------------------------- General Information: -------------------------------\n\n\n";
        peerState += "Peer\'s Capacity: " + (double) storage.getAvailableStorage() / 1000 + " kB\n";
        peerState += "Peer\'s used Storage: " + (double) storage.getTotalStorage() / 1000 + " kB\n";

        return peerState;
    }

    public void setInitiator(boolean isInitiator) {
        Peer.isInitiator = isInitiator;
    }
}
