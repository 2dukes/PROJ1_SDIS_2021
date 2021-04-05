package peer;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PeerFile implements Serializable {
    private String fileId;
    private int peerId;
    private int replicationDegree;
    private List<Chunk> chunks;
    private List<Integer> peersBackingUp;
    private String path;

    public PeerFile(String path, int replicationDegree, int peerId) throws IOException, NoSuchAlgorithmException  {
        this.replicationDegree = replicationDegree;
        this.path = path;
        this.peerId = peerId;
        Path fPath = Paths.get(path);
        UserPrincipal fileOwner = Files.getOwner(fPath, LinkOption.NOFOLLOW_LINKS);
        this.fileId = fPath.getFileName().toString();
        long lastModifiedMs = fPath.toFile().lastModified();

        this.createIdentifier(fPath.getFileName().toString(), fileOwner.getName(), new Date(lastModifiedMs));
        this.createChunks(fPath);

        this.peersBackingUp = new ArrayList<>();
    }

    // https://www.geeksforgeeks.org/sha-256-hash-in-java/
    private void createIdentifier(String fileName, String owner, Date dateModified) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String seed = fileName + owner + dateModified.toString();
        byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));
        
        BigInteger number = new BigInteger(1, hash);  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
        while (hexString.length() < 32)
            hexString.insert(0, '0');  
        
        this.fileId = hexString.toString(); 
    }

    public String getId() {
        return this.fileId;
    }

    public int getReplicationDegree() {
        return this.replicationDegree;
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

    public void createChunks(Path filePath) throws IOException {
        this.chunks = new ArrayList<>();
        byte[] fileData;
        fileData = Files.readAllBytes(filePath);
        int fileSize = (int) Files.size(filePath);

        int i, chunkNo = 0, chunkSize = 64000;

        for(i = 0; i < fileSize; i += 64000) {
            byte[] chunkData;
            if (fileSize - i >= chunkSize) { // if it's not the last chunk
                chunkData = Arrays.copyOf(fileData,chunkSize);
                fileData = Arrays.copyOfRange(fileData, chunkSize, fileSize - i);
                this.chunks.add(new Chunk(this.fileId, chunkNo++, chunkData, this.replicationDegree));
                if (chunkData.length == chunkSize && i + chunkSize >= fileSize) {
                    this.chunks.add(new Chunk(this.fileId, chunkNo++, new byte[0], this.replicationDegree));
                }
            }
            else { // last chunk
                chunkData = Arrays.copyOf(fileData,fileSize - i);
                fileData = new byte[0];
                this.chunks.add(new Chunk(this.fileId, chunkNo++, chunkData, this.replicationDegree));
            }
        }
    }

    public List<Integer> getPeersBackingUp() {
        return peersBackingUp;
    }

    public void addPeerBackingUp(int peerId) {
        this.peersBackingUp.add(peerId);
    }

    public void removePeerBackingUp(int peerId) {
        for (int i = 0; i < peersBackingUp.size(); i++) {
            if (peersBackingUp.get(i) == peerId) {
                peersBackingUp.remove(i);
                return;
            }
        }
    }

    public String getPath() {
        return path;
    }
}
