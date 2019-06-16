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
            if (cartoonUrl == null) {
                logger.warn(String.format("Url not available for cartoon %s.", cartoonName));
                continue;
            }
            Map<String, String> episodeList = pageWalker.getCartoonPaths(cartoonUrl);
            logger.info(String.format("Found %d episodes for %s", episodeList.size(), cartoonName));

            EpisodeTracker episodeTracker = applicationContext.getBean(EpisodeTracker.class, cartoonName);

            episodeList.forEach((episodeName, episodeUrl) -> {
                if (episodeTracker.shouldDownloadFile(episodeName)) {
                    DownloadWorker downloadWorker = applicationContext.getBean(DownloadWorker.class, episodeTracker);
                    FutureTask<Boolean> downloadTask = new FutureTask<>(() -> {
                        String videoUrl = videoFinder.findVideoUrl(episodeUrl);
                        if (videoUrl != null) {
                            logger.debug(String.format("Episode name: %s, Video url: %s", episodeName, videoUrl));
                            return downloadWorker.downloadVideo(episodeName, videoUrl);
                        } else {
                            logger.warn(String.format("Could not find download url for: %s.", episodeUrl));
                            return false;
                        }
                    });
                    executorService.schedule(downloadTask, 0, TimeUnit.SECONDS);
                } else {
                    logger.info(String.format("%s was already downloaded.", episodeName));
                }
            });
        }
    }
}
