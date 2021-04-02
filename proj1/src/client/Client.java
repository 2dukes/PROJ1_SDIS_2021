package client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import peer.*;

public class Client {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        chooseOption();
    }

    public static void displayMenu() {
        System.out.println("Please choose an action:\n");
        System.out.println("1. Backup a file");
        System.out.println("2. Restore a file");
        System.out.println("3. Delete a file");
        System.out.println("4. Manage local service storage");
        System.out.println("5. Retrieve local service state information\n");
        System.out.print("Option: ");
    }

    public static void chooseOption() throws IOException, NoSuchAlgorithmException {
        int option;
        do {
            Scanner scanner = new Scanner(System.in);
            displayMenu();
            option = scanner.nextInt();
        } while (option < 1 || option > 5);

        switch (option) {
            case 1:
                backupFileMenu();
                break;
            default:
                break;
        }
    }

    public static void backupFileMenu() throws IOException, NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the filepath: ");
        String filepath = scanner.nextLine();

        System.out.print("Please enter the desired replication degree: ");
        int replicationDegree = scanner.nextInt();

        PeerFile peerFile = new PeerFile(filepath, replicationDegree, Peer.id);
        Peer.storage.addFile(peerFile);
    }
}
