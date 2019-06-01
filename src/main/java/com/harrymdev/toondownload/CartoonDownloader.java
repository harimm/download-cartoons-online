package com.harrymdev.toondownload;

import com.harrymdev.toondownload.config.BaseConfig;
import com.harrymdev.toondownload.worker.DownloadManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CartoonDownloader {
    private static final Logger logger = Logger.getLogger(CartoonDownloader.class);

    public static void main(String[] args) {
        logger.info("Initializing Application Context.");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BaseConfig.class);
        context.refresh();
        logger.info("Application Context Initialization Complete.");

        DownloadManager downloadManager = context.getBean(DownloadManager.class);
        downloadManager.downloadCartoons();
    }

}
