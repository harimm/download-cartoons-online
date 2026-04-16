package dev.harimohan.app.toondownload;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CartoonDownloaderTest {

    @Test
    public void shouldStartAndShutdownWithoutThrowing() {
        assertDoesNotThrow(() -> CartoonDownloader.main(new String[0]));
    }
}
