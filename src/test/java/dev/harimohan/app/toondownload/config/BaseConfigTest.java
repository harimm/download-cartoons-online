package dev.harimohan.app.toondownload.config;

import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BaseConfigTest {

    @Test
    public void shouldProvideSingletonBeans() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(BaseConfig.class);
            context.refresh();

            ScheduledExecutorService executorA = context.getBean("getExecutorService", ScheduledExecutorService.class);
            ScheduledExecutorService executorB = context.getBean("getExecutorService", ScheduledExecutorService.class);
            JSONParser parserA = context.getBean("getJsonParser", JSONParser.class);
            JSONParser parserB = context.getBean("getJsonParser", JSONParser.class);

            assertNotNull(executorA);
            assertNotNull(parserA);
            assertSame(executorA, executorB);
            assertSame(parserA, parserB);

            executorA.shutdownNow();
        }
    }
}
