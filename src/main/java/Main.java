import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final byte[] PONG_RESPONSE = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] UNKNOWN_COMMAND = "-ERR unknown command\r\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] ECHO_ARG_ERROR =
            "-ERR wrong number of arguments for 'echo' command\r\n".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {
        int port = parsePort(args);

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));

            while (true) {
                Socket client = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(client));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Failed to bind to port " + port + ": " + e.getMessage());
        }
    }

    private static int parsePort(String[] args) {
        int defaultPort = 6379;
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {
                    return defaultPort;
                }
            }
        }
        return defaultPort;
    }

    private static void handleClient(Socket client) {
        try (
                Socket socket = client;
                InputStream rawIn = new BufferedInputStream(socket.getInputStream());
                OutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                List<String> command = readCommand(rawIn);
                if (command == null) {
                    return;
                }

                if (!command.isEmpty() && "PING".equalsIgnoreCase(command.get(0))) {
                    out.write(PONG_RESPONSE);
                } else if (!command.isEmpty() && "ECHO".equalsIgnoreCase(command.get(0))) {
                    if (command.size() < 2) {
                        out.write(ECHO_ARG_ERROR);
                    } else {
                        out.write(bulkString(command.get(1)));
                    }
                } else {
                    out.write(UNKNOWN_COMMAND);
                }
                out.flush();
            }
        } catch (IOException ignored) {
            // Client disconnected or sent malformed input; close socket quietly.
        }
    }

    private static List<String> readCommand(InputStream in) throws IOException {
        int first = in.read();
        if (first == -1) {
            return null;
        }

        if (first == '*') {
            int elementCount = readIntegerLine(in);
            List<String> parts = new ArrayList<>(elementCount);
            for (int i = 0; i < elementCount; i++) {
                int marker = in.read();
                if (marker != '$') {
                    throw new IOException("Expected bulk string");
                }
                int length = readIntegerLine(in);
                byte[] data = readExact(in, length);
                expectCrlf(in);
                parts.add(new String(data, StandardCharsets.UTF_8));
            }
            return parts;
        }

        String inline = (char) first + readLineRemainder(in);
        String trimmed = inline.trim();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }

        String[] tokens = trimmed.split("\\s+");
        List<String> parts = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            parts.add(token);
        }
        return parts;
    }

    private static int readIntegerLine(InputStream in) throws IOException {
        return Integer.parseInt(readLineRemainder(in));
    }

    private static String readLineRemainder(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        while (true) {
            int current = in.read();
            if (current == -1) {
                throw new IOException("Unexpected EOF");
            }
            if (current == '\r') {
                int next = in.read();
                if (next != '\n') {
                    throw new IOException("Invalid line ending");
                }
                return line.toString();
            }
            line.append((char) current);
        }
    }

    private static byte[] readExact(InputStream in, int length) throws IOException {
        byte[] data = new byte[length];
        int offset = 0;
        while (offset < length) {
            int bytesRead = in.read(data, offset, length - offset);
            if (bytesRead == -1) {
                throw new IOException("Unexpected EOF");
            }
            offset += bytesRead;
        }
        return data;
    }

    private static void expectCrlf(InputStream in) throws IOException {
        if (in.read() != '\r' || in.read() != '\n') {
            throw new IOException("Expected CRLF");
        }
    }

    private static byte[] bulkString(String value) {
        byte[] content = value.getBytes(StandardCharsets.UTF_8);
        String header = "$" + content.length + "\r\n";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        byte[] response = new byte[headerBytes.length + content.length + 2];

        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(content, 0, response, headerBytes.length, content.length);
        response[response.length - 2] = '\r';
        response[response.length - 1] = '\n';
        return response;
    }
}
