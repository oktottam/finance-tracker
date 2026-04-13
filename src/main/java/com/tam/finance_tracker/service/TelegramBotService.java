package com.tam.finance_tracker.service;

import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.repository.CategoryRepository;
import com.tam.finance_tracker.repository.UserRepository;
import com.tam.finance_tracker.util.VNCharacterUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository; // Inject thêm để gọi hàm tìm kiếm thông minh

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
        // 1. Kiểm tra nếu là tin nhắn văn bản
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info(">>> [BOT] Nhận được tin nhắn: {}", update.getMessage().getText()); // Dời vào đây
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            if (messageText.startsWith("/start")) {
                handleStartCommand(chatId, messageText);
            } else {
                handleQuickLog(chatId, messageText);
            }
        }
        // 2. Kiểm tra nếu là sự kiện bấm nút
        else if (update.hasCallbackQuery()) {
            log.info(">>> [BOT] Nhận được sự kiện bấm nút: {}", update.getCallbackQuery().getData());
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callback) {
        String data = callback.getData(); // Format: "LEARN|keyword|amount|categoryId"
        String chatId = callback.getMessage().getChatId().toString();
        Integer messageId = callback.getMessage().getMessageId();

        String[] parts = data.split("\\|");
        if (parts.length < 4 || !parts[0].equals("LEARN"))
            return;

        String rawKeyword = parts[1];
        long amount = Long.parseLong(parts[2]);
        Long categoryId = Long.parseLong(parts[3]);

        // 1. Tìm User & Category thực tế từ DB
        userRepository.findByTelegramChatId(chatId).ifPresent(user -> {
            categoryRepository.findById(categoryId).ifPresent(category -> {

                // HÀNH ĐỘNG 1: "Dạy" Bot (Lưu mapping cá nhân)
                categoryService.learnNewMapping(user, category, rawKeyword);

                // HÀNH ĐỘNG 2: Lưu Transaction vừa rồi
                transactionService.createQuickTransaction(user, amount, category);

                // HÀNH ĐỘNG 3: Thông báo & Xóa Menu nút bấm (Edit Message)
                String finalMsg = String.format("✅ Đã nhớ '%s' là %s\n💰 Đã chi: *%,d VNĐ*",
                        rawKeyword, category.getName(), amount);

                editMessage(chatId, messageId, finalMsg);

                // Trả lời callback để mất cái icon "đồng hồ cát" trên Telegram của Tâm
                answerCallback(callback.getId());
            });
        });
    }

    private void editMessage(String chatId, Integer messageId, String newText) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId);
        edit.setMessageId(messageId);
        edit.setText(newText);
        edit.setParseMode("Markdown");
        try {
            execute(edit);
        } catch (TelegramApiException e) {
            log.error("Lỗi edit message: {}", e.getMessage());
        }
    }

    private void answerCallback(String callbackId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Lỗi answer callback: {}", e.getMessage());
        }
    }

    private void handleQuickLog(String chatId, String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "^/?(?:chi|tieu|pay)?\\s*(.+?)\\s+(\\d+)([kK])?$",
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CHARACTER_CLASS);

        java.util.regex.Matcher matcher = pattern.matcher(text.trim());

        if (matcher.find()) {
            String rawCategory = matcher.group(1).trim();
            long rawValue = Long.parseLong(matcher.group(2));
            String unit = matcher.group(3);
            long amount = (StringUtils.hasText(unit) || rawValue < 1000) ? rawValue * 1000 : rawValue;

            // 1. Lấy User từ chatId TRƯỚC để dùng cho việc tìm kiếm cá nhân hóa
            userRepository.findByTelegramChatId(chatId).ifPresentOrElse(user -> {

                // 2. SỬA TẠI ĐÂY: Dùng findByKeywordForUser thay vì findByKeyword
                categoryService.findByKeywordForUser(rawCategory, user).ifPresentOrElse(category -> {

                    transactionService.createQuickTransaction(user, amount, category);

                    String successMsg = String.format("✅ Đã ghi nhận: *%,d VNĐ*\n📂 Hạng mục: %s %s",
                            amount, category.getIcon(), category.getName());
                    sendSimpleMessage(chatId, successMsg);

                }, () -> {
                    // Nếu cả từ điển chung và riêng đều không có -> Hiện menu chọn
                    sendCategorySelection(chatId, rawCategory, amount);
                });

            }, () -> {
                sendSimpleMessage(chatId, "⚠️ Tâm ơi, tài khoản chưa được liên kết. Gõ /start để bắt đầu nhé!");
            });
        } else {
            log.info("Tin nhắn không khớp format log: {}", text);
        }
    }

    private void sendCategorySelection(String chatId, String rawCategory, long amount) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("😅 Hạng mục '" + rawCategory + "' mới quá, Tâm chọn giúp mình thuộc loại nào nhé:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Lấy tất cả category từ DB lên để tạo nút
        categoryRepository.findAll().forEach(cat -> {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(cat.getIcon() + " " + cat.getName());

            // Data gửi về khi Tâm bấm nút: ACTION|KEYWORD|AMOUNT|CAT_ID
            // Ví dụ: LEARN|an sang|26000|1
            btn.setCallbackData("LEARN|" + rawCategory + "|" + amount + "|" + cat.getId());

            rows.add(List.of(btn));
        });

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message); // Gửi tin nhắn kèm nút bấm
        } catch (TelegramApiException e) {
            log.error("Lỗi gửi menu chọn category: {}", e.getMessage());
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