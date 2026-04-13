package com.tam.finance_tracker.consumer;

import com.tam.finance_tracker.config.RabbitMQConfig;
import com.tam.finance_tracker.document.TransactionDocument;
import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.repository.TransactionSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng Lombok để tự động tạo logger, Tâm sẽ có log.info(), log.error()... xài cực tiện
public class TransactionConsumer {
    private final TransactionRepository transactionRepository;
    private final TransactionSearchRepository searchRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeTransaction(String transactionId) {
        log.info("Đang nhận tin nhắn đồng bộ giao dịch: {}", transactionId);
        
        transactionRepository.findById(transactionId).ifPresent(transaction -> {
            TransactionDocument doc = TransactionDocument.builder()
                    .id(transaction.getId())
                    .description(transaction.getDescription())
                    .amount(transaction.getAmount())
                    .category(transaction.getCategory() != null ? transaction.getCategory().getName() : "Khác")
                    .createdAt(transaction.getCreatedAt())
                    .build();
            
            searchRepository.save(doc); // Đẩy vào Elasticsearch thành công!
            log.info("Đã đồng bộ thành công giao dịch {} sang Elasticsearch", transactionId);
        });
    }
}