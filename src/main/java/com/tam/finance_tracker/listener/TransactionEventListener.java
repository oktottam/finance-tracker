package com.tam.finance_tracker.listener;

import com.tam.finance_tracker.config.RabbitMQConfig;
import com.tam.finance_tracker.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final RabbitTemplate rabbitTemplate;

    // phase = AFTER_COMMIT đảm bảo DB đã lưu xong mới bắn sang RabbitMQ
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        String id = event.getTransactionId();
        log.info("📢 Đã commit thành công giao dịch {}. Đang gửi sang RabbitMQ...", id);
        
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY, 
                id
            );
            log.info("✅ Gửi RabbitMQ thành công!");
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi tin nhắn sang RabbitMQ: {}", e.getMessage());
            // Tại đây Tâm có thể thêm logic lưu vào bảng Retry nếu cần
        }
    }
}