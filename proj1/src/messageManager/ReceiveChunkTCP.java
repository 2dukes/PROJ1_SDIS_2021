package messageManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import macros.Macros;
import peer.Chunk;
import peer.Peer;

public class ReceiveChunkTCP extends MessageManager {
    private int chunkNo;
    private String fileId;

    public ReceiveChunkTCP(String fileId) {
        super();
        this.fileId = fileId;
    }

    @Override
    public void parseSpecificParameters() {
        this.chunkNo = Integer.parseInt(this.header[4]);
    }

    @Override
    public void run() {
        try {
            int TCP_Port = Peer.storage.getFilePort(this.fileId);
            ServerSocket serverSocket = new ServerSocket(TCP_Port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // https://stackoverflow.com/questions/2878867/how-to-send-an-array-of-bytes-over-a-tcp-connection-java-programming
                DataInputStream inBuf = new DataInputStream(clientSocket.getInputStream());

                int msgLen = inBuf.readInt();
                this.data = new byte[msgLen];
                inBuf.readFully(data);

                parseMessage(this.data);
                parseCommonParameters();
                parseSpecificParameters();

                Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body, 0);
                Peer.storage.addRestoredChunk(chunk);

                // System.out.println("Received Message: " + new String(this.data));


                System.out.format("RECEIVED CHUNK [TCP] version=%s senderId=%s fileId=%s chunkNo=%s\n",
                        this.version, this.senderId, this.fileId, this.chunkNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
