package com.tam.finance_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // Kích hoạt tính năng tự động quản lý createdAt và updatedAt trong BaseEntity
@EnableAsync    // Chạy tác vụ ngầm
@EnableCaching  // Bật bộ nhớ đệm Redis
@EnableScheduling // Bật tính năng lập lịch (dùng cho việc xóa file sau khi export)
public class FinanceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}

}
