package dev.harimohan.app.toondownload.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpisodeStatusTest {

    @Test
    void shouldContainExpectedValuesInOrder() {
        EpisodeStatus[] values = EpisodeStatus.values();

        assertEquals(3, values.length);
        assertEquals(EpisodeStatus.Pending, values[0]);
        assertEquals(EpisodeStatus.Downloaded, values[1]);
        assertEquals(EpisodeStatus.Ignored, values[2]);
    }
}
