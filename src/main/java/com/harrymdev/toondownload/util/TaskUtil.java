package com.harrymdev.toondownload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskUtil {
    private static final Logger logger = LoggerFactory.getLogger(TaskUtil.class);

    public static <T> void waitUntilComplete(Collection<Future<T>> futures) {
        if (futures != null) {
            for (Future<T> future : futures) {
                waitUntilComplete(future);
            }
        }
    }

    private static <T> void waitUntilComplete(Future<T> future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to get status of download task!", e);
        }
    }
}
