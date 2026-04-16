package dev.harimohan.app.toondownload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TestSupport {
    private static final Map<HttpServer, ExecutorService> SERVER_EXECUTORS = new IdentityHashMap<>();

    private TestSupport() {
    }

    public static Map<String, String> setSystemProperties(Map<String, String> updates) {
        Map<String, String> previous = new HashMap<>();
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            previous.put(entry.getKey(), System.getProperty(entry.getKey()));
            System.setProperty(entry.getKey(), entry.getValue());
        }
        return previous;
    }

    public static void restoreSystemProperties(Map<String, String> previousValues) {
        for (Map.Entry<String, String> entry : previousValues.entrySet()) {
            if (entry.getValue() == null) {
                System.clearProperty(entry.getKey());
            } else {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    public static HttpServer startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("test-http-server");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);
        SERVER_EXECUTORS.put(server, executor);
        server.start();
        return server;
    }

    public static void stopServer(HttpServer server) {
        if (server != null) {
            server.stop(0);
            ExecutorService executor = SERVER_EXECUTORS.remove(server);
            if (executor != null) {
                executor.shutdownNow();
            }
        }
    }

    public static String baseUrl(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    public static HttpHandler textResponse(int status, String body) {
        return exchange -> write(exchange, status, body.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpHandler bytesResponse(int status, byte[] body) {
        return exchange -> write(exchange, status, body);
    }

    private static void write(HttpExchange exchange, int status, byte[] bytes) throws IOException {
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
