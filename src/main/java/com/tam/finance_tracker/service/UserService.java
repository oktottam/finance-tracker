package com.tam.finance_tracker.service;

import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional // Rất quan trọng để đảm bảo atomicity khi update database
    public String generateTelegramLink(String username) {
        // 1. Sinh một chuỗi ngẫu nhiên không quá dài
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Tìm và Lưu token này vào User tương ứng
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tâm ơi, không tìm thấy User: " + username));

        user.setVerificationToken(token);
        userRepository.save(user);

        // Sửa từ tam_finance_tracker_bot thành tam_finance_monitor_bot
        return "https://t.me/tam_finance_monitor_bot?start=" + token;
    }

    @Transactional
    public void linkTelegramAccount(String token, String chatId) {
        // 1. Tìm User dựa trên token mà Bot nhận được từ lệnh /start
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn, Tâm ơi!"));

        // 2. Cập nhật chatId và xóa token (để mã này không dùng lại được nữa)
        user.setTelegramChatId(chatId);
        user.setVerificationToken(null);

        userRepository.save(user);

        log.info("User {} đã liên kết thành công với Telegram ChatID: {}", user.getUsername(), chatId);
    }
}