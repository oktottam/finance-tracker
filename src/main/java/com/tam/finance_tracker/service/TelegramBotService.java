package com.tam.finance_tracker.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelegramBotService {
    // Spring Boot sẽ tự động map giá trị từ application.yml vào đây
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String message) {
        // Kiểm tra xem token đã được load chưa để tránh lỗi NullPointerException
        if (botToken == null || chatId == null) {
            log.error("Telegram Config chưa được cấu hình trong application.yml!");
            return;
        }

        String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s", 
                                    botToken, chatId, message);
        try {
            restTemplate.getForObject(url, String.class);
            log.info("Đã gửi thông báo tới Bot giám sát thành công!");
        } catch (Exception e) {
            log.error("Không thể gửi thông báo tới Bot giám sát: {}", e.getMessage());
        }
    }
}
