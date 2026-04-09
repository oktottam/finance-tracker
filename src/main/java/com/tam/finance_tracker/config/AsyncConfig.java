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

    @Bean(name = "exportTaskExecutor")
    public Executor exportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Giữ ít nhất 2 luồng luôn sẵn sàng
        executor.setMaxPoolSize(5);  // Tối đa 5 luồng nếu dồn dập request
        executor.setQueueCapacity(100); // Đợi tối đa 100 task trong hàng chờ
        executor.setThreadNamePrefix("Tâm-Export-");
        executor.initialize();
        return executor;
    }
}
