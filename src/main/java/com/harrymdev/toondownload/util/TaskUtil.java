package com.harrymdev.toondownload.util;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskUtil {
    private static Logger logger = Logger.getLogger(TaskUtil.class);

    public static <T> void waitUntilComplete(Collection<Future<T>> futures) {
        if (futures != null) {
            for (Future future : futures) {
                waitUntilComplete(future);
            }
        }
    }

    private static void waitUntilComplete(Future future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to get status of download task!", e);
        }
    }
}
