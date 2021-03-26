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

public class File implements Serializable {
    private String fileId;
    private int peerId;
    private Path filePath;
    private int replicationDegree;
    private List<Chunk> chunks;

    public File(String path, int replicationDegree, int peerId) throws IOException, NoSuchAlgorithmException  {
        this.replicationDegree = replicationDegree;
        this.peerId = peerId;
        Path fPath = Paths.get(path);
        this.filePath = fPath;
        UserPrincipal fileOwner = Files.getOwner(fPath, LinkOption.NOFOLLOW_LINKS);
        this.fileId = fPath.getFileName().toString();
        long lastModifiedMs = fPath.toFile().lastModified();

        this.createIdentifier(filePath.getFileName().toString(), fileOwner.getName(), new Date(lastModifiedMs));
        this.createChunks();
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
    
    public Path getFilePath() {
        return this.filePath;
    }

    public int getReplicationDegree() {
        return this.replicationDegree;
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

    public void createChunks() throws IOException {
        this.chunks = new ArrayList<>();
        byte[] fileData;
        fileData = Files.readAllBytes(this.filePath);
        int fileSize = (int) Files.size(this.filePath);

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

}
