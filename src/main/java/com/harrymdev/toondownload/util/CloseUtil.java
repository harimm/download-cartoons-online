package com.harrymdev.toondownload.util;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
    private static Logger logger = Logger.getLogger(CloseUtil.class);
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.warn("Error closing object. Instance of " + closeable.getClass(), e);
            }
        }
    }
}
