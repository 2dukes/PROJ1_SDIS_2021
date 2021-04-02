package messageManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class MessageManager implements Runnable {
    protected byte[] data;
    protected final byte CR = 0x0D;
    protected final byte LF = 0x0A;

    protected String[] header;
    protected byte[] body;
    protected String version;
    protected int senderId;
    protected String fileId;

    public MessageManager(byte[] data) {
        this.data = data;
        parseCommonParameters();
        parseSpecificParameters();
    }

    // <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public void parseMessage(byte[] data) throws Exception {
        // CR - 0x0D
        // LF - 0X0A

        byte currentByte = data[0];
        byte nextByte = data[1];
        int index = 2;
        while(index < data.length) {
            if(currentByte == CR && nextByte == LF && data[index + 2] == CR && data[index + 3] == LF) {
                Map<String, byte[]> message = new HashMap<>();
                byte[] headerBytes = Arrays.copyOfRange(data, 0, index);
                this.body = Arrays.copyOfRange(data, index + 4, data.length);
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
