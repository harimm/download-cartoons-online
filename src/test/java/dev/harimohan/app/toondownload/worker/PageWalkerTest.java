package dev.harimohan.app.toondownload.worker;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Map;

import static dev.harimohan.app.toondownload.TestSupport.baseUrl;
import static dev.harimohan.app.toondownload.TestSupport.restoreSystemProperties;
import static dev.harimohan.app.toondownload.TestSupport.setSystemProperties;
import static dev.harimohan.app.toondownload.TestSupport.startServer;
import static dev.harimohan.app.toondownload.TestSupport.stopServer;
import static dev.harimohan.app.toondownload.TestSupport.textResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PageWalkerTest {

    private HttpServer server;
    private Map<String, String> previousSystemProperties;

    @AfterEach
    public void tearDown() {
        if (server != null) {
            stopServer(server);
        }
        if (previousSystemProperties != null) {
            restoreSystemProperties(previousSystemProperties);
        }
    }

    @Test
    public void shouldReturnEpisodeMapForValidPage() throws Exception {
        server = startServer();
        String base = baseUrl(server);
        String html = "<div><p><a href=\"" + base + "cartoon\">Show</a></p>"
                + "<div><ul>"
                + "<li><a href=\"/ep2\">Episode 2</a></li>"
                + "<li><a href=\"/ep1\">Episode 1</a></li>"
                + "</ul></div></div>";
        server.createContext("/cartoon", textResponse(200, html));

        previousSystemProperties = setSystemProperties(Map.of("toon_download.base.url", base));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            PageWalker pageWalker = context.getBean(PageWalker.class);
            Map<String, String> episodes = pageWalker.getCartoonPaths("cartoon");

            assertNotNull(episodes);
            assertEquals(2, episodes.size());
            assertEquals("/ep1", episodes.get("Episode 1"));
            assertEquals("/ep2", episodes.get("Episode 2"));
        }
    }

    @Test
    public void shouldReturnEmptyMapOnNonOkStatus() throws Exception {
        server = startServer();
        String base = baseUrl(server);
        server.createContext("/cartoon", textResponse(404, "not found"));
        previousSystemProperties = setSystemProperties(Map.of("toon_download.base.url", base));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            PageWalker pageWalker = context.getBean(PageWalker.class);
            Map<String, String> episodes = pageWalker.getCartoonPaths("cartoon");

            assertNotNull(episodes);
            assertEquals(0, episodes.size());
        }
    }

    @Test
    public void shouldReturnNullWhenPageDoesNotMatchExpectedStructure() throws Exception {
        server = startServer();
        String base = baseUrl(server);
        server.createContext("/cartoon", textResponse(200, "<html><body><h1>No episode list</h1></body></html>"));
        previousSystemProperties = setSystemProperties(Map.of("toon_download.base.url", base));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            PageWalker pageWalker = context.getBean(PageWalker.class);
            Map<String, String> episodes = pageWalker.getCartoonPaths("cartoon");

            assertNull(episodes);
        }
    }

    @Configuration
    private static class TestConfig {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public PageWalker pageWalker() {
            return new PageWalker();
        }
    }
}
