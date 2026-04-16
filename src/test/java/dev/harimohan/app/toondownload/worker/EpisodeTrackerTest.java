package dev.harimohan.app.toondownload.worker;

import dev.harimohan.app.toondownload.model.EpisodeStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static dev.harimohan.app.toondownload.TestSupport.restoreSystemProperties;
import static dev.harimohan.app.toondownload.TestSupport.setSystemProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpisodeTrackerTest {

    @TempDir
    private Path tempDir;

    private Map<String, String> previousSystemProperties;

    @AfterEach
    public void tearDown() {
        if (previousSystemProperties != null) {
            restoreSystemProperties(previousSystemProperties);
        }
    }

    @Test
    public void shouldFailWhenDownloadFolderPropertyIsMissing() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            assertThrows(BeanCreationException.class, () -> context.getBean(EpisodeTracker.class, "missing"));
        }
    }

    @Test
    public void shouldMarkEpisodeAsDownloadedAfterAddingToList() throws Exception {
        String folder = tempDir.resolve("tracker").toString() + File.separator;
        previousSystemProperties = setSystemProperties(Map.of(
                "toon_download.download.sample.target.path.root", folder
        ));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            EpisodeTracker tracker = context.getBean(EpisodeTracker.class, "sample");

            assertEquals(EpisodeStatus.Pending, tracker.shouldDownloadFile("ep-1"));
            tracker.addEpisodeToList("ep-1");
            assertEquals(EpisodeStatus.Downloaded, tracker.shouldDownloadFile("ep-1"));

            String persisted = Files.readString(Path.of(folder, "episodeList"));
            assertTrue(persisted.contains("ep-1"));
        }
    }

    @Test
    public void shouldLoadIgnoreListEntries() throws Exception {
        String folder = tempDir.resolve("trackerIgnore").toString() + File.separator;
        Files.createDirectories(Path.of(folder));
        Files.writeString(Path.of(folder, "ignoreList"), "ep-ignored\n");

        previousSystemProperties = setSystemProperties(new HashMap<>(Map.of(
                "toon_download.download.sampleIgnore.target.path.root", folder
        )));

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestConfig.class);
            context.refresh();

            EpisodeTracker tracker = context.getBean(EpisodeTracker.class, "sampleIgnore");
            assertEquals(EpisodeStatus.Ignored, tracker.shouldDownloadFile("ep-ignored"));
            assertEquals(EpisodeStatus.Pending, tracker.shouldDownloadFile("ep-new"));
        }
    }

    @Configuration
    private static class TestConfig {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        @Scope("prototype")
        public EpisodeTracker episodeTracker(String cartoonName) {
            return new EpisodeTracker(cartoonName);
        }
    }
}
