package peer;

import channels.MCChannel;
import channels.MDBChannel;
import channels.MDRChannel;
import macros.Macros;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import java.util.Arrays;
import java.util.stream.Stream;

public class Peer {
    public static int id = 1;
    public static String version;
    public static String accessPoint;
    public static MCChannel mcChannel;
    public static MDBChannel mdbChannel;
    public static MDRChannel mdrChannel;
    public static PeerStorage storage;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 9) {
            System.err.println("Usage: java Peer <id> <version> <access point> <IP-MC> <Port-MC> <IP-MDB> <Port-MDB> <IP-MDR> <Port-MDR>");
            return;
        }
        try {
            id = Integer.parseInt(args[0]);
            version = args[1];
            accessPoint = args[2];
            mcChannel = new MCChannel(args[3], Integer.parseInt(args[4]));
            mdbChannel = new MDBChannel(args[5], Integer.parseInt(args[6]));
            mdrChannel = new MDRChannel(args[7], Integer.parseInt(args[8]));
        } catch (Exception e) {
            System.err.println("Bad arguments usage: " + e.getMessage());
        }

        storage = new PeerStorage();

        PeerFile peerFile = new PeerFile("src/files/hello.txt", 2, id);
        storage.peerFiles.add(peerFile);

        deserializeStorage();

        // https://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage));
        // deserializeStorage();


        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the filepath: ");
        String filepath = scanner.nextLine();

        System.out.print("Please enter the desired replication degree: ");
        int replicationDegree = scanner.nextInt();

        PeerFile peerFile1 = new PeerFile(filepath, replicationDegree, Peer.id);

        Peer.storage.addFile(peerFile1);

        Chunk chunk = storage.getChunks().get(0);

        String messageStr = "1.0 PUTCHUNK 1 jhflsdrohjfdserk7934nfkhkuf0xjodiede$joifer 1 2" + Macros.CR + Macros.LF + Macros.CR + Macros.LF;

        byte[] header = messageStr.getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(header);
        outputStream.write(chunk.getData());

        byte[] message = outputStream.toByteArray();

        mcChannel.send(message);

        mcChannel.run();
    }

    public Peer() {

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


}
