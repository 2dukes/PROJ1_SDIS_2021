import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Peer {
    private static int id;
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

        File file = new File("hello.txt", 2, id);
        storage.files = new ArrayList<>();
        storage.addFile(file);

        serializeStorage();
    }

    public Peer() {

    }

    public static boolean serializeStorage() {
        try {
            FileOutputStream file = new FileOutputStream("peerStorage.ser");
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(storage);
            out.close();
            file.close();
            return true;
        }
        catch(IOException e) {
            System.err.println("Exception was caught: " + e.toString());
            return false;
        }
    }

    public static boolean deserializeStorage() {
        try  {
            FileInputStream file = new FileInputStream("PeerStorage.ser");
            ObjectInputStream in = new ObjectInputStream(file);
            storage = (PeerStorage) in.readObject();
            in.close();
            file.close();
            return true;
        }

        catch(Exception e) {
            System.err.println("Exception was caught: " + e.getMessage());
            return false;
        }
    }


}
