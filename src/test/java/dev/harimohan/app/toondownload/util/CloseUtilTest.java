package dev.harimohan.app.toondownload.util;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CloseUtilTest {

    @Test
    public void shouldIgnoreNullCloseable() {
        assertDoesNotThrow(() -> CloseUtil.close(null));
    }

    @Test
    public void shouldCloseProvidedInstance() throws IOException {
        Closeable closeable = mock(Closeable.class);

        CloseUtil.close(closeable);

        verify(closeable).close();
    }

    @Test
    public void shouldSwallowIoException() {
        Closeable closeable = mock(Closeable.class);
        doThrow(new IOException("boom")).when(closeable).close();

        assertDoesNotThrow(() -> CloseUtil.close(closeable));
    }
}
