package messageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import macros.Macros;
import peer.Peer;

public abstract class MessageManager implements Runnable {
    protected byte[] data;

    protected String[] header;
    protected byte[] body;
    protected String version;
    protected int senderId;
    protected String fileId;

    public MessageManager(byte[] data) {
        this.data = data;
        try {
            parseMessage(this.data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        parseCommonParameters();
        parseSpecificParameters();
    }

    public MessageManager() { }

    // <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public void parseMessage(byte[] data) throws Exception {
        // CR - 0x0D
        // LF - 0X0A

        byte currentByte = data[0];
        byte nextByte = data[1];
        int index = 2;
        while(index < data.length - 2) {

            if(currentByte == Macros.CR && nextByte == Macros.LF && data[index + 1] == Macros.CR && data[index + 2] == Macros.LF) {
                byte[] headerBytes = Arrays.copyOfRange(data, 0, index);
                this.body = Arrays.copyOfRange(data, index + 3, data.length);
                this.header = new String(headerBytes).trim().split("\\s+");

                if(this.header.length < 4)
                    throw new Exception("Poorly formatted header!");

                return;
            }

            currentByte = data[index];
            nextByte = data[++index];
        }

        throw new Exception("Failed to parse message!");
    }

    // [<Version> <MessageType> <SenderId> <FileId>] <ChunkNo> <ReplicationDeg> <CRLF>
    public void parseCommonParameters() {
        this.version = this.header[0];
        this.senderId = Integer.parseInt(this.header[2]);
        this.fileId = this.header[3];
    }

    public abstract void parseSpecificParameters();

    @Override
    public abstract void run();
}
