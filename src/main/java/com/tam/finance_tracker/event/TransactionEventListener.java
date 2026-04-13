package com.tam.finance_tracker.event;

import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final TransactionRepository transactionRepository;
    private final TelegramBotService telegramBotService;

    // Quan trọng: Chỉ bắn thông báo SAU KHI database đã commit thành công
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info(">>> [EVENT] Đang xử lý gửi thông báo cho giao dịch: {}", event.getTransactionId());

        transactionRepository.findById(event.getTransactionId()).ifPresent(t -> {
            if (t.getUser() != null && t.getUser().getTelegramChatId() != null) {
                String message = String.format(
                    "💰 *BIẾN ĐỘNG SỐ DƯ*\n" +
                    "--------------------------\n" +
                    "💵 Số tiền: *%,.0f VNĐ*\n" +
                    "📂 Hạng mục: %s %s\n" +
                    "📝 Nội dung: %s\n" +
                    "--------------------------\n" +
                    "✅ Giao dịch đã được ghi nhận thành công!",
                    t.getAmount(),
                    t.getCategory().getIcon(), t.getCategory().getName(),
                    t.getDescription()
                );

                telegramBotService.sendSimpleMessage(t.getUser().getTelegramChatId(), message);
            }
        });
    }
}