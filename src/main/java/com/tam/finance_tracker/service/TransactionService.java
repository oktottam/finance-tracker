package com.tam.finance_tracker.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.config.RabbitMQConfig;
import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.repository.CreditCardRepository;
import com.tam.finance_tracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final RabbitTemplate rabbitTemplate; // Công cụ bắn tin nhắn sang RabbitMQ

    @Transactional
    public Transaction createTransaction(Transaction transaction, Long cardId) {
        // 1. Kiểm tra thẻ có tồn tại không
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ tín dụng!"));

        // 2. Logic Senior: Kiểm tra hạn mức (Demo đơn giản)
        // Trong thực tế, Tâm sẽ tính tổng Transaction của tháng này và so sánh với limitAmount
        
        transaction.setCard(card);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 3. (Giai đoạn sau) Gửi tin nhắn qua RabbitMQ để đồng bộ Elasticsearch tại đây
        // Bắn tin nhắn Async sang RabbitMQ để đồng bộ Elasticsearch
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE, 
            RabbitMQConfig.ROUTING_KEY, 
            savedTransaction.getId() // Gửi ID của giao dịch đi là đủ gọn (Simple is the best)
        );
        return savedTransaction;
    }
}
