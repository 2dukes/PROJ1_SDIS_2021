public class MDBChannel {
    private String IP;
    private int port;

    public MDBChannel(String IP, int port) {
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
