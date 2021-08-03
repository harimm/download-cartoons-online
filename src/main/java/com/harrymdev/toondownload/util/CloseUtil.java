package com.harrymdev.toondownload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
    private static final Logger logger = LoggerFactory.getLogger(CloseUtil.class);

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.warn(String.format("Error closing object. Instance of %s", closeable.getClass().getCanonicalName()), e);
            }
        }
    }
}
