package channels;

import messageManager.RemovedPutChunk;

import java.net.SocketException;
import java.net.UnknownHostException;

public class PutChunkMDBChannel extends Channel {
    private String fileIdToSearch;
    private int chunkNoToSearch;
    public PutChunkMDBChannel(String IP, int port, String fileIdToSearch, int chunkNoToSearch) throws SocketException, UnknownHostException {
        super(IP, port);
        this.fileIdToSearch = fileIdToSearch;
        this.chunkNoToSearch = chunkNoToSearch;
    }

    @Override
    public void handleMessageType(byte[] data) {
        String msgType = new String(data).trim().split("\\s+")[1];
        switch (msgType) {
            case "PUTCHUNK":
                peer.Peer.scheduledThreadPoolExecutor.execute(new RemovedPutChunk(data, this.fileIdToSearch, this.chunkNoToSearch));
                break;
            default:
                System.err.println("PutChunkMDB Channel message type error:" + msgType);
        }
    }
}
