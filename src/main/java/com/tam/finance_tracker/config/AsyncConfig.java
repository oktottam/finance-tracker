package com.tam.finance_tracker.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Số luồng luôn sẵn sàng
        executor.setMaxPoolSize(10); // Số luồng tối đa khi quá tải
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Tâm-Report-");
        executor.initialize();
        return executor;
    }
}
