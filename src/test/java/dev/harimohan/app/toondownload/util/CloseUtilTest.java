package dev.harimohan.app.toondownload.util;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloseUtilTest {

    @Test
    public void shouldIgnoreNullCloseable() {
        assertDoesNotThrow(() -> CloseUtil.close(null));
    }

    @Test
    public void shouldCloseProvidedInstance() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Closeable closeable = () -> closed.set(true);

        CloseUtil.close(closeable);

        assertTrue(closed.get());
    }

    @Test
    public void shouldSwallowIoException() {
        Closeable closeable = () -> {
            throw new IOException("boom");
        };

        assertDoesNotThrow(() -> CloseUtil.close(closeable));
    }
}
