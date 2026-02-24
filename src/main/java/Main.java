import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        int port = 6379;

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));

            while (true) {
                Socket client = serverSocket.accept();
                client.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to bind to port " + port + ": " + e.getMessage());
        }
    }
}
