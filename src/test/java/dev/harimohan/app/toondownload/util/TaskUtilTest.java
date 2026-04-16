package dev.harimohan.app.toondownload.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TaskUtilTest {

    @Test
    public void shouldHandleNullCollection() {
        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(null));
    }

    @Test
    public void shouldWaitForSuccessfulFutures() throws Exception {
        Future<String> firstFuture = mock(Future.class);
        Future<String> secondFuture = mock(Future.class);
        List<Future<String>> futures = List.of(firstFuture, secondFuture);

        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(futures));

        verify(firstFuture).get();
        verify(secondFuture).get();
    }

    @Test
    public void shouldSwallowExecutionAndInterruptedExceptions() throws Exception {
        Future<String> executionFailure = mock(Future.class);
        Future<String> interruptedFailure = mock(Future.class);
        doThrow(new ExecutionException(new RuntimeException("fail"))).when(executionFailure).get();
        doThrow(new InterruptedException("interrupted")).when(interruptedFailure).get();

        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(Collections.singletonList(executionFailure)));
        assertDoesNotThrow(() -> TaskUtil.waitUntilComplete(Collections.singletonList(interruptedFailure)));

        verify(executionFailure).get();
        verify(interruptedFailure).get();
    }
}
