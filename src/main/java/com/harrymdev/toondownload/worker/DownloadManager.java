package com.harrymdev.toondownload.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DownloadManager {
    private static final Logger logger = Logger.getLogger(DownloadManager.class);

    private static final String CARTOON_URL_PROPERTY = "toon_download.download.%s.url";

    @Value("#{'${toon_download.cartoons_to_download}'.split(',')}")
    private List<String> cartoonsToDownload;

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
            Map<String, String> episodeList = pageWalker.getCartoonPaths(cartoonUrl);
            logger.info("Found " + episodeList.size() + " episodes for " + cartoonName);

            EpisodeTracker episodeTracker = applicationContext.getBean(EpisodeTracker.class, cartoonName);

            episodeList.forEach((key, value) -> {
                if (episodeTracker.shouldDownloadFile(key)) {
                    DownloadWorker downloadWorker = applicationContext.getBean(DownloadWorker.class, episodeTracker);
                    FutureTask<Boolean> task = new FutureTask<>(() -> {
                        String videoUrl = videoFinder.findVideoUrl(value);
                        logger.debug("Episode name: " + key + ", Video url: " + videoUrl);
                        return downloadWorker.downloadVideo(key, videoUrl);
                    });
                    executorService.schedule(task, 0, TimeUnit.SECONDS);
                } else {
                    logger.info(key + " was already downloaded.");
                }
            });
        }
    }
}
