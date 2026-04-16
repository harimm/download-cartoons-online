package dev.harimohan.app.toondownload;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CartoonDownloaderTest {

    @Test
    void shouldStartAndShutdownWithoutThrowing() {
        assertDoesNotThrow(() -> CartoonDownloader.main(new String[0]));
    }
}
