package com.tam.finance_tracker.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.domain.Category;
import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.dto.TransactionRequest;
import com.tam.finance_tracker.repository.CategoryRepository;
import com.tam.finance_tracker.repository.CreditCardRepository;
import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CreditCardRepository creditCardRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CategoryRepository categoryRepository;

    // Trong TransactionService.java
    @Transactional
    public Transaction createTransaction(Transaction transaction, Long cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ!"));

        transaction.setCard(card);
        Transaction saved = transactionRepository.save(transaction);

        // Thay vì gửi trực tiếp, ta bắn một Event nội bộ của Spring
        // Event này sẽ được xử lý sau khi Transaction COMMIT
        applicationEventPublisher.publishEvent(new TransactionCreatedEvent(saved.getId()));

        return saved;
    }

    @Transactional
    public Transaction createTransactionFromRequest(TransactionRequest request) {
        // 1. Tìm Category từ categoryId trong request
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        // 2. Map dữ liệu vào Entity
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);

        // 3. Gọi hàm createTransaction cũ (nơi có logic lưu Card và bắn Event)
        return this.createTransaction(transaction, request.getCardId());
    }
}
