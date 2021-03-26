import java.io.*;
import java.security.NoSuchAlgorithmException;

public class Peer {
    private static int id = 1;
    private static String version;
    private static String accessPoint;
    private static MCChannel mcChannel;
    private static MDBChannel mdbChannel;
    private static MDRChannel mdrChannel;
    private static PeerStorage storage;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        /*if (args.length != 9) {
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
        }*/

        storage = new PeerStorage();

        PeerFile peerFile = new PeerFile("hello.txt", 2, id);
        storage.peerFiles.add(peerFile);

        deserializeStorage();



        // https://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage));
        // deserializeStorage();
    }

    public Peer() {

    }

    public static void serializeStorage() {
        try {
            String fileName = Peer.id + "/peerStorage.ser";

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
            String fileName = Peer.id + "/peerStorage.ser";
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
