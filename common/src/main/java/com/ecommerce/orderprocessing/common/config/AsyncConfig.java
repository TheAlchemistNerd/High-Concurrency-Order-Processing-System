package com.ecommerce.orderprocessing.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class AsyncConfig {

    @Value("${app.virtual-threads.executor.core-pool-size:10}")
    private int corePoolSize;

    @Value("${app.virtual-threads.executor.max-pool-size:100}")
    private int maxPoolSize;

    @Bean
    public ExecutorService virtualThreadExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofVirtual().factory()
        );
    }
}
