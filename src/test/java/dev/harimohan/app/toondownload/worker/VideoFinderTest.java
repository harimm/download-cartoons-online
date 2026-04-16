package dev.harimohan.app.toondownload.worker;

import com.sun.net.httpserver.HttpServer;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.harimohan.app.toondownload.TestSupport.baseUrl;
import static dev.harimohan.app.toondownload.TestSupport.bytesResponse;
import static dev.harimohan.app.toondownload.TestSupport.startServer;
import static dev.harimohan.app.toondownload.TestSupport.stopServer;
import static dev.harimohan.app.toondownload.TestSupport.textResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VideoFinderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            stopServer(server);
        }
    }

    @Test
    void shouldFindVideoUrlUsingMethodOne() throws Exception {
        server = startServer();
        String html = "<html><body>"
                + "<div id=\"jwplayer-0\"></div>"
                + "<script>setup({file: \"https://cdn.example/video.mp4\", type: \"mp4\"});</script>"
                + "</body></html>";
        server.createContext("/episode", textResponse(200, html));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            VideoFinder finder = context.getBean(VideoFinder.class);
            String result = finder.findVideoUrl(baseUrl(server) + "episode");
            assertEquals("https://cdn.example/video.mp4", result);
        }
    }

    @Test
    void shouldFindVideoUrlUsingMethodTwoFallback() throws Exception {
        server = startServer();
        String base = baseUrl(server);
        String html = "<html><body><div class=\"playerpro\">"
                + "<script>var a = 1;</script>"
                + "<script>url: \"" + base + "api\", data: {link: \"token123X\"}</script>"
                + "</div></body></html>";

        server.createContext("/episode", textResponse(200, html));
        server.createContext("/api", textResponse(200, "https://stream.example/final.mp4"));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            VideoFinder finder = context.getBean(VideoFinder.class);
            String result = finder.findVideoUrl(base + "episode");
            assertEquals("https://stream.example/final.mp4", result);
        }
    }

    @Test
    void shouldReturnNullWhenEpisodeRequestIsNotSuccessful() throws Exception {
        server = startServer();
        server.createContext("/episode", bytesResponse(404, new byte[0]));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            VideoFinder finder = context.getBean(VideoFinder.class);
            String result = finder.findVideoUrl(baseUrl(server) + "episode");
            assertNull(result);
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        JSONParser jsonParser() {
            return new JSONParser();
        }

        @Bean
        VideoFinder videoFinder() {
            return new VideoFinder();
        }
    }
}
