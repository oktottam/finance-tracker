package com.tam.finance_tracker.config;

import com.tam.finance_tracker.service.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        // Khởi tạo API với DefaultBotSession (sử dụng Long Polling)
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        
        try {
            // Đăng ký instance Bot của Tâm vào hệ thống
            botsApi.registerBot(telegramBotService);
            System.out.println(">>> [SUCCESS] Bot @" + telegramBotService.getBotUsername() + " đã kết nối thành công!");
        } catch (TelegramApiException e) {
            System.err.println(">>> [ERROR] Lỗi khi đăng ký Bot: " + e.getMessage());
            throw e;
        }
        
        return botsApi;
    }
}