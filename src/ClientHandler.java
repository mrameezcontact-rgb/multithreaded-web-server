import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    private static final Path WEB_ROOT = Paths.get("D:\\multithreaded web server\\MyWebServer\\public");

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true)
        ) {
            // 1. Request Line Parse
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            System.out.println("Received Request: " + requestLine);
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;

            String method = requestParts[0];
            String fileRequested = requestParts[1];

            if (fileRequested.equals("/favicon.ico")) {
                return; // Ignore favicon request to avoid cluttering logs
            }

            // Headers ko read karke skip karna
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                // Headers skip
            }

            // Just GET method support
            if (!method.equals("GET")) {
                sendError(writer, outputStream, 405, "Method Not Allowed");
                return;
            }

            // Agar root '/' ho toh index.html load
            if (fileRequested.equals("/")) {
                fileRequested = "/index.html";
            }

            // 2. File System (WebRoot) se file dhoondna
            Path filePath = WEB_ROOT.resolve(fileRequested.substring(1));
            System.out.println("Looking for file at: " + filePath.toAbsolutePath());

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] fileData = Files.readAllBytes(filePath);
                String contentType = getContentType(fileRequested);

                // 3. Response Builder
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: " + contentType);
                writer.println("Content-Length: " + fileData.length);
                writer.println(); // Blank line
                writer.flush();

                outputStream.write(fileData);
                outputStream.flush();
                System.out.println("Served: " + fileRequested + " (200 OK)");
            } else {
                sendError(writer, outputStream, 404, "Not Found");
                System.out.println("File not found: " + fileRequested + " (404 Not Found)");
            }

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html") || fileRequested.endsWith(".htm")) {
            return "text/html";
        } else if (fileRequested.endsWith(".css")) {
            return "text/css";
        } else if (fileRequested.endsWith(".js")) {
            return "application/javascript";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "text/plain";
    }

    private void sendError(PrintWriter writer, OutputStream outputStream, int statusCode, String statusText) throws IOException {
        String body = "<html><body><h1>" + statusCode + " " + statusText + "</h1><p>The requested resource could not be found on this server.</p></body></html>";
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: text/html");
        writer.println("Content-Length: " + body.getBytes().length);
        writer.println();
        writer.println(body);
        writer.flush();
    }
}