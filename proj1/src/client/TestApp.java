package client;

import peer.RMIService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLOutput;

public class TestApp {
    public static void main(String[] args) {
        if(args.length > 4) {
            System.err.println("ERROR: App format must be: App <host>/<peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        try {

            String[] firstArgs = args[0].split("/");
            String host = firstArgs[0];
            String accessPoint = firstArgs[1];
            String subProtocol = args[1];
            System.out.println("RMI 1");
            Registry registry = LocateRegistry.getRegistry(host);
            RMIService initiatorPeer = (RMIService) registry.lookup(accessPoint);
            System.out.println("RMI 2");

            String path;
            switch (subProtocol) {
                case "BACKUP":
                    System.out.println("Backing up...");
                    path = args[2].trim();
                    int replicationDeg = Integer.parseInt(args[3]);
                    initiatorPeer.backup(path, replicationDeg);
                    break;
                case "RECLAIM":
                    int amountDiskSpace = Integer.parseInt(args[2]);
                    initiatorPeer.reclaim(amountDiskSpace);
                    break;
                case "DELETE":
                    System.out.println("Deleting...");
                    path = args[2].trim();
                    initiatorPeer.delete(path);
                    break;
                case "RESTORE":
                    path = args[2].trim();
                    initiatorPeer.restore(path);
                    break;
                default:
                    throw new Exception("Wrong arguments [sub_protocol = " + subProtocol + "]");
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }

    }
}
