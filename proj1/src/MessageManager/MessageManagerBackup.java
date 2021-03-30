package MessageManager;

public class MessageManagerBackup extends MessageManager {
    public MessageManagerBackup(byte[] data) {
        super(data);
    }

    @Override
    public void run() {
        String msgType = new String(this.data).trim().split(" ")[1];

        switch (msgType) {
            case "PUTCHUNK":
                // ...
                break;
            case "STORED":
                // ...
                break;
        }
    }
}
