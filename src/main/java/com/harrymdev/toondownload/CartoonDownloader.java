package com.harrymdev.toondownload;

import com.harrymdev.toondownload.config.BaseConfig;
import com.harrymdev.toondownload.util.CloseUtil;
import com.harrymdev.toondownload.worker.DownloadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CartoonDownloader {
    private static final Logger logger = LoggerFactory.getLogger(CartoonDownloader.class);

    public static void main(String[] args) {
        logger.info("Initializing Application Context.");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BaseConfig.class);
        context.refresh();
        logger.info("Application Context Initialization Complete.");

        try {
            logger.info("Starting Cartoon downloader.");
            DownloadManager downloadManager = context.getBean(DownloadManager.class);
            downloadManager.downloadCartoons();
        } finally {
            logger.info("Shutting down application.");
            CloseUtil.close(context);
        }
    }

}
