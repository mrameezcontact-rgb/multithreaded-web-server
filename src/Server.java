import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 50; // Max 50 concurrent worker threads

    public static void main(String[] args) {
        // Thread Pool (Worker Threads)
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("=========================================");
            System.out.println(" Web Server started successfully!");
            System.out.println(" Listening on http://localhost:" + PORT);
            System.out.println("=========================================");

            while (true) {
                // Client connection accept (Listener Thread / Accept Queue)
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                // Task ThreadPool mein submit kar diya (Worker Thread handle karega)
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
