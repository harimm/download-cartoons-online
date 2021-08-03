package com.harrymdev.toondownload.worker;

import com.harrymdev.toondownload.model.EpisodeStatus;
import com.harrymdev.toondownload.util.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

    private static final String CARTOON_URL_PROPERTY = "toon_download.download.%s.url";

    @Value("#{'${toon_download.cartoons_to_download}'.split(',')}")
    private List<String> cartoonsToDownload;
    @Value("${toon_download.download.wait_until_series_complete}")
    private boolean waitUntilComplete;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PageWalker pageWalker;
    @Autowired
    private VideoFinder videoFinder;
    @Autowired
    private ScheduledExecutorService executorService;

    public void downloadCartoons() {
        for (String cartoonName : cartoonsToDownload) {
            String cartoonUrl = applicationContext.getEnvironment().getProperty(String.format(CARTOON_URL_PROPERTY, cartoonName));
            if (cartoonUrl == null) {
                logger.warn(String.format("Url not available for cartoon %s.", cartoonName));
                continue;
            }
            Map<String, String> episodeList = pageWalker.getCartoonPaths(cartoonUrl);
            if (episodeList == null) {
                logger.error(String.format("Episode list not found. Skipping cartoon - %s.", cartoonName));
                continue;
            }
            logger.info(String.format("Found %d episodes for %s", episodeList.size(), cartoonName));

            EpisodeTracker episodeTracker = applicationContext.getBean(EpisodeTracker.class, cartoonName);

            List<Future<Boolean>> downloadTasks = new ArrayList<>();

            episodeList.forEach((episodeName, episodeUrl) -> {
                EpisodeStatus episodeStatus = episodeTracker.shouldDownloadFile(episodeName);
                if (EpisodeStatus.Pending.equals(episodeStatus)) {
                    DownloadWorker downloadWorker = applicationContext.getBean(DownloadWorker.class, episodeTracker);
                    FutureTask<Boolean> downloadTask = new FutureTask<>(() -> {
                        String videoUrl = videoFinder.findVideoUrl(episodeUrl);
                        if (videoUrl != null) {
                            logger.debug(String.format("Episode name: %s, Video url: %s", episodeName, videoUrl));
                            boolean status = downloadWorker.downloadVideo(episodeName, videoUrl);
                            if (!status) {
                                logger.error(String.format("Failed to download episode from url %s.", episodeUrl));
                            }
                            return false;
                        } else {
                            logger.warn(String.format("Could not find download url for: %s.", episodeUrl));
                            return false;
                        }
                    });
                    downloadTasks.add(downloadTask);
                    executorService.schedule(downloadTask, 0, TimeUnit.SECONDS);
                } else if (EpisodeStatus.Downloaded.equals(episodeStatus)) {
                    logger.info(String.format("%s was already downloaded.", episodeName));
                } else {
                    logger.info(String.format("%s is ignored.", episodeName));
                }
            });
            if (waitUntilComplete) {
                TaskUtil.waitUntilComplete(downloadTasks);
            }
        }
    }
}
