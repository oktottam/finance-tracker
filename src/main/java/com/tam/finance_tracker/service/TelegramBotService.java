package com.tam.finance_tracker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // Để inject ObjectMapper tự động
public class TelegramBotService {
    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper; // Dùng để convert Map sang JSON String
    private final UserRepository userRepository; // Dùng để tìm User khi họ chat với Bot
    private final UserService userService; // Dùng để xử lý liên kết tài khoản khi nhận lệnh /start 

    public void sendMessage(String chatId, String message, Object replyMarkup) {
        if (botToken == null) {
            log.error("Telegram Token chưa được cấu hình!");
            return;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", message);
        request.put("parse_mode", "Markdown");

        try {
            if (replyMarkup != null) {
                // Ép kiểu markup sang JSON String
                request.put("reply_markup", objectMapper.writeValueAsString(replyMarkup));
            }
            restTemplate.postForObject(url, request, String.class);
            log.info("Đã gửi thông báo tới Telegram thành công!");
        } catch (Exception e) {
            log.error("Lỗi gửi Telegram: {}", e.getMessage());
        }
    }

    public void notifyApproval(String adminChatId, Budget budget) {
        String text = String.format(
                "🔔 *Yêu cầu duyệt ngân sách*\n" +
                        "📂 Hạng mục: %s\n" +
                        "💰 Số tiền: %,.0f VNĐ\n" +
                        "⏳ Cần: %d người duyệt",
                budget.getCategory().getName(),
                budget.getLimitAmount(),
                budget.getRequiredApprovals());

        Map<String, Object> markup = Map.of(
                "inline_keyboard", List.of(
                        List.of(
                                Map.of("text", "✅ Duyệt", "callback_data", "APPROVE_" + budget.getId()),
                                Map.of("text", "❌ Từ chối", "callback_data", "REJECT_" + budget.getId()))));

        sendMessage(adminChatId, text, markup);
    }

    @Transactional
    public void handleStartCommand(String chatId, String messageText) {
        // 1. Kiểm tra nếu tin nhắn bắt đầu bằng /start
        if (messageText.startsWith("/start")) {
            // Tách lấy username (ví dụ: "/start tam_backend" -> "tam_backend")
            String[] parts = messageText.split(" ");

            if (parts.length < 2) {
                sendMessage(chatId, "⚠️ Vui lòng truy cập từ ứng dụng Finance Tracker để link tài khoản!", null);
                return;
            }

            String username = parts[1];

            // 2. Tìm User trong DB và cập nhật chatId
            userRepository.findByUsername(username).ifPresentOrElse(user -> {
                user.setTelegramChatId(chatId);
                userRepository.save(user); // Lưu "địa chỉ" nhà Tâm vào DB

                sendMessage(chatId, "✅ Chào **" + username + "**! Tài khoản của Tâm đã được liên kết thành công. " +
                        "Giờ Tâm có thể nhận báo cáo ETL và duyệt ngân sách ngay tại đây.", null);
            }, () -> {
                sendMessage(chatId, "❌ Không tìm thấy người dùng: " + username, null);
            });
        }
    }

    @Transactional
    public void handleLinkAccount(String chatId, String incomingText) {
        if (incomingText.startsWith("/start ")) {
            String verificationToken = incomingText.substring(7); // Lấy phần mã sau "/start "

            // Giả sử Tâm lưu mã này vào một trường 'verificationToken' trong bảng User lúc
            // đăng ký
            userRepository.findByVerificationToken(verificationToken).ifPresentOrElse(user -> {
                user.setTelegramChatId(chatId);
                user.setVerificationToken(null); // Xóa mã sau khi dùng xong để bảo mật
                userRepository.save(user);

                sendMessage(chatId, "🎉 Chúc mừng **" + user.getUsername() + "**!\n" +
                        "Tài khoản của Tâm đã được liên kết thành công. " +
                        "Từ giờ Tâm sẽ nhận được thông báo biến động số dư và duyệt ngân sách tại đây.", null);
            }, () -> {
                sendMessage(chatId, "❌ Mã xác thực không hợp lệ hoặc đã hết hạn.", null);
            });
        }
    }

    public void onUpdateReceived(String messageText, String chatId) {
        if (messageText.startsWith("/start ")) {
            String token = messageText.substring(7); // Cắt bỏ chữ "/start "
            try {
                userService.linkTelegramAccount(token, chatId);
                sendMessage(chatId, "✅ Liên kết thành công! Từ giờ Tâm có thể nhận báo cáo ETL tại đây.", null);
            } catch (Exception e) {
                sendMessage(chatId, "❌ Lỗi: " + e.getMessage(), null);
            }
        }
    }
}