package dev.harimohan.app.toondownload.worker;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static dev.harimohan.app.toondownload.TestSupport.baseUrl;
import static dev.harimohan.app.toondownload.TestSupport.bytesResponse;
import static dev.harimohan.app.toondownload.TestSupport.startServer;
import static dev.harimohan.app.toondownload.TestSupport.stopServer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownloadWorkerTest {

    @TempDir
    Path tempDir;

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            stopServer(server);
        }
    }

    @Test
    void shouldDownloadVideoAndMarkEpisode() throws Exception {
        server = startServer();
        byte[] content = "video-content".getBytes(StandardCharsets.UTF_8);
        server.createContext("/ok", bytesResponse(200, content));

        EpisodeTracker tracker = mock(EpisodeTracker.class);
        when(tracker.getDownloadFolder()).thenReturn(tempDir.toString() + "\\");

        DownloadWorker worker = new DownloadWorker(tracker);
        boolean result = worker.downloadVideo("episode1", baseUrl(server) + "ok");

        assertTrue(result);
        assertTrue(tempDir.resolve("episode1.mp4").toFile().exists());
        verify(tracker).addEpisodeToList("episode1");
    }

    @Test
    void shouldReturnFalseWhenHttpStatusIsNotOk() {
        try {
            server = startServer();
            server.createContext("/missing", bytesResponse(404, new byte[0]));

            EpisodeTracker tracker = mock(EpisodeTracker.class);
            when(tracker.getDownloadFolder()).thenReturn(tempDir.toString() + "\\");

            DownloadWorker worker = new DownloadWorker(tracker);
            boolean result = worker.downloadVideo("episode2", baseUrl(server) + "missing");

            assertFalse(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnFalseOnInvalidUrl() {
        EpisodeTracker tracker = mock(EpisodeTracker.class);
        when(tracker.getDownloadFolder()).thenReturn(tempDir.toString() + "\\");

        DownloadWorker worker = new DownloadWorker(tracker);
        boolean result = worker.downloadVideo("episode3", "not-a-valid-url");

        assertFalse(result);
    }
}
