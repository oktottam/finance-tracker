package com.tam.finance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.domain.TransactionCategory;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    // Tự động sinh query tìm giao dịch theo ID của thẻ
    List<Transaction> findByCardId(Long cardId);

    // Tìm giao dịch theo danh mục (Shopping, Food...)
    List<Transaction> findByCategory(TransactionCategory category);
    
}
