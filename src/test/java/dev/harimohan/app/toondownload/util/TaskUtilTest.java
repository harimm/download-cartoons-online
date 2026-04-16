package dev.harimohan.app.toondownload.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TaskUtilTest {

    @Test
    public void shouldHandleNullCollection() {
        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(null));
    }

    @Test
    public void shouldWaitForSuccessfulFutures() {
        List<Future<String>> futures = Arrays.asList(
                CompletableFuture.completedFuture("one"),
                CompletableFuture.completedFuture("two")
        );

        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(futures));
    }

    @Test
    public void shouldSwallowExecutionAndInterruptedExceptions() {
        Future<String> executionFailure = CompletableFuture.failedFuture(new ExecutionException(new RuntimeException("fail")));
        Future<String> interruptedFailure = new InterruptedFuture<>();

        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(Collections.singletonList(executionFailure)));
        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(Collections.singletonList(interruptedFailure)));
    }

    private static class InterruptedFuture<T> extends CompletableFuture<T> {
        @Override
        public T get() throws InterruptedException {
            throw new InterruptedException("interrupted");
        }
    }
}
