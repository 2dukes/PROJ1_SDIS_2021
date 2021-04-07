package macros;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Random;

public interface Macros {
    byte CR = 0x0D;
    byte LF = 0x0A;
    int NUM_THREADS = 128;
    int TCP_PORT = 6969;
    //int TCP_PORT = findAvailablePort();

    static int findAvailablePort() {
        int port = 0;
        // For ServerSocket port number 0 means that the port number is automatically allocated.
        try (ServerSocket socket = new ServerSocket(0)) {
            // Disable timeout and reuse address after closing the socket.
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (port > 0)
            return port;

        return 6969;
    }
}



