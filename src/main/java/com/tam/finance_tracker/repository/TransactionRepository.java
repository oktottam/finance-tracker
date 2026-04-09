package com.tam.finance_tracker.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.domain.TransactionCategory;
import com.tam.finance_tracker.dto.CategorySumProjection;
import com.tam.finance_tracker.dto.MonthlySummary;

import jakarta.persistence.QueryHint;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    // Tự động sinh query tìm giao dịch theo ID của thẻ
    List<Transaction> findByCardId(Long cardId);

    // Tìm giao dịch theo danh mục (Shopping, Food...)
    List<Transaction> findByCategory(TransactionCategory category);
    
    @Query(value = "SELECT category as category, SUM(amount) as amount " +
                   "FROM transactions " +
                   "WHERE owner_username = :username " +
                   "AND EXTRACT(MONTH FROM transaction_date) = :month " +
                   "AND EXTRACT(YEAR FROM transaction_date) = :year " +
                   "GROUP BY category", nativeQuery = true)
    List<CategorySumProjection> getRawSummary(@Param("username") String username, 
                                              @Param("month") int month, 
                                              @Param("year") int year);

    // Một hàm helper để gộp dữ liệu thành đối tượng Summary hoàn chỉnh
    default MonthlySummary calculateSummary(String username, int month, int year) {
        List<CategorySumProjection> results = getRawSummary(username, month, year);
        
        BigDecimal total = results.stream()
                .map(CategorySumProjection::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> breakdown = results.stream()
                .collect(Collectors.toMap(
                    CategorySumProjection::getCategory,
                    CategorySumProjection::getAmount
                ));

        return new MonthlySummary(total, breakdown);
    }

    @QueryHints(value = {
        @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100"),
        @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "false"),
        @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_READONLY, value = "true")
    })
    @Query("select t from Transaction t")
    Stream<Transaction> streamAllTransactions();
}
