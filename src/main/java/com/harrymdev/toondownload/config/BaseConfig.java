package com.harrymdev.toondownload.config;

import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@PropertySource({"classpath:application.properties"})
@ComponentScan("com.harrymdev.toondownload")
public class BaseConfig {
    @Value("${toon_download.download.pool.size}")
    private Integer poolSize;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ScheduledExecutorService getExecutorService() {
        return Executors.newScheduledThreadPool(poolSize);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JSONParser getJsonParser() {
        return new JSONParser();
    }

}
