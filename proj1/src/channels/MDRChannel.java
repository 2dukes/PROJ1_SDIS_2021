package channels;

public class MDRChannel {
    private String IP;
    private int port;

    public MDRChannel(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }
}
