package com.tam.finance_tracker.service;

import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;

    @Value("${TELEGRAM_BOT_USERNAME:tam_finance_monitor_bot}")
    private String botUsername;

    private final UserRepository userRepository;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    // --- XỬ LÝ NHẬN TIN NHẮN (Long Polling) ---
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            if (messageText.startsWith("/start")) {
                handleStartCommand(chatId, messageText);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            sendSimpleMessage(chatId, "Hệ thống đã ghi nhận lệnh: " + callbackData);
        }
    }

    private void handleStartCommand(String chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            sendSimpleMessage(chatId, "⚠️ Vui lòng liên kết tài khoản từ ứng dụng Web của Tâm!");
            return;
        }

        String token = parts[1];
        userRepository.findByVerificationToken(token).ifPresentOrElse(user -> {
            user.setTelegramChatId(chatId);
            user.setVerificationToken(null);
            userRepository.save(user);
            sendSimpleMessage(chatId, "✅ Chào *" + user.getUsername() + "*, tài khoản đã liên kết thành công!");
        }, () -> sendSimpleMessage(chatId, "❌ Mã xác thực không hợp lệ hoặc đã hết hạn."));
    }

    // --- CÁC METHOD GỬI TIN NHẮN ---

    /**
     * Gửi tin nhắn đơn giản (Dùng nội bộ Service)
     */
    public void sendSimpleMessage(String chatId, String text) {
        sendMessage(chatId, text, null);
    }

    /**
     * Method cũ để fix lỗi compile cho các class khác (AsyncExportService,
     * WeeklyReportScheduler)
     */
    public void sendMessage(String chatId, String message, Object replyMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode("Markdown");

        if (replyMarkup instanceof ReplyKeyboard) {
            sendMessage.setReplyMarkup((ReplyKeyboard) replyMarkup);
        }

        try {
            execute(sendMessage);
            log.info(">>> Đã gửi tin nhắn tới Telegram chat: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Lỗi gửi tin nhắn Telegram: {}", e.getMessage());
        }
    }

    /**
     * Thông báo duyệt ngân sách
     */
    public void notifyApproval(String adminChatId, Budget budget) {
        String text = String.format("🔔 *Duyệt ngân sách*\n📂 Hạng mục: %s\n💰 Số tiền: %,.0f VNĐ",
                budget.getCategory().getName(), budget.getLimitAmount());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton approveBtn = new InlineKeyboardButton();
        approveBtn.setText("✅ Duyệt");
        approveBtn.setCallbackData("APPROVE_" + budget.getId());

        InlineKeyboardButton rejectBtn = new InlineKeyboardButton();
        rejectBtn.setText("❌ Từ chối");
        rejectBtn.setCallbackData("REJECT_" + budget.getId());

        rowInline.add(approveBtn);
        rowInline.add(rejectBtn);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        sendMessage(adminChatId, text, markupInline);
    }

    public void sendPhoto(String chatId, InputStream imageStream, String fileName, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("Markdown");
        sendPhoto.setPhoto(new InputFile(imageStream, fileName));

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Lỗi gửi ảnh Telegram: {}", e.getMessage());
        }
    }
}