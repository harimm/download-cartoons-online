package dev.harimohan.app.toondownload.worker;

import dev.harimohan.app.toondownload.model.EpisodeStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownloadManagerTest {
    private DownloadManager manager;
    private ApplicationContext applicationContext;
    private Environment environment;
    private PageWalker pageWalker;
    private VideoFinder videoFinder;
    private ScheduledExecutorService executorService;

    private EpisodeTracker episodeTracker;
    private DownloadWorker downloadWorker;

    @BeforeEach
    void setUp() {
        manager = new DownloadManager();
        applicationContext = mock(ApplicationContext.class);
        environment = mock(Environment.class);
        pageWalker = mock(PageWalker.class);
        videoFinder = mock(VideoFinder.class);
        episodeTracker = mock(EpisodeTracker.class);
        downloadWorker = mock(DownloadWorker.class);
        executorService = Executors.newSingleThreadScheduledExecutor();

        when(applicationContext.getEnvironment()).thenReturn(environment);

        setField(manager, "applicationContext", applicationContext);
        setField(manager, "pageWalker", pageWalker);
        setField(manager, "videoFinder", videoFinder);
        setField(manager, "executorService", executorService);

        setField(manager, "cartoonsToDownload", List.of("sample"));
        setField(manager, "waitUntilComplete", true);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Test
    void shouldSkipCartoonWhenUrlPropertyIsMissing() {
        when(environment.getProperty("toon_download.download.sample.url")).thenReturn(null);

        manager.downloadCartoons();

        verify(pageWalker, never()).getCartoonPaths(anyString());
    }

    @Test
    void shouldSkipWhenEpisodeListIsNull() {
        when(environment.getProperty("toon_download.download.sample.url")).thenReturn("series");
        when(pageWalker.getCartoonPaths("series")).thenReturn(null);

        manager.downloadCartoons();

        verify(pageWalker, times(1)).getCartoonPaths("series");
        verify(episodeTracker, never()).shouldDownloadFile(anyString());
    }

    @Test
    void shouldProcessPendingEpisodesAndSkipOthers() {
        when(environment.getProperty("toon_download.download.sample.url")).thenReturn("series");

        Map<String, String> episodeMap = new LinkedHashMap<>();
        episodeMap.put("ep-pending", "/pending");
        episodeMap.put("ep-downloaded", "/downloaded");
        episodeMap.put("ep-ignored", "/ignored");

        when(pageWalker.getCartoonPaths("series")).thenReturn(episodeMap);
        when(applicationContext.getBean(EpisodeTracker.class, "sample")).thenReturn(episodeTracker);
        when(applicationContext.getBean(DownloadWorker.class, episodeTracker)).thenReturn(downloadWorker);
        when(episodeTracker.shouldDownloadFile("ep-pending")).thenReturn(EpisodeStatus.Pending);
        when(episodeTracker.shouldDownloadFile("ep-downloaded")).thenReturn(EpisodeStatus.Downloaded);
        when(episodeTracker.shouldDownloadFile("ep-ignored")).thenReturn(EpisodeStatus.Ignored);
        when(videoFinder.findVideoUrl("/pending")).thenReturn("http://video/pending.mp4");
        when(downloadWorker.downloadVideo("ep-pending", "http://video/pending.mp4")).thenReturn(true);

        manager.downloadCartoons();

        verify(episodeTracker, times(3)).shouldDownloadFile(anyString());
        verify(videoFinder, times(1)).findVideoUrl("/pending");
        verify(downloadWorker, times(1)).downloadVideo("ep-pending", "http://video/pending.mp4");
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = DownloadManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
