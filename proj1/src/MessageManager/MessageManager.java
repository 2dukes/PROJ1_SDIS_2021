package MessageManager;

public abstract class MessageManager implements Runnable {
    protected byte[] data;

    public MessageManager(byte[] data) {
        this.data = data;
    }

    @Override
    public abstract void run();
}
