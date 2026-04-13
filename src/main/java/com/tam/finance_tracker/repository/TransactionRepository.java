package com.tam.finance_tracker.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.tam.finance_tracker.domain.Category;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.dto.CategorySumProjection;
import com.tam.finance_tracker.dto.MonthlySummary;

import jakarta.persistence.QueryHint;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

        List<Transaction> findByCardId(Long cardId);

        List<Transaction> findByCategory(Category category);

        // FIX 1: Sửa Native Query cho khớp với tên cột trong Postgres
        // Cột 'category' trong Hibernate map thành 'category_id'
        // Cột 'owner_username' trong User entity, nhưng ở đây cần join qua bảng users
        @Query(value = "SELECT c.name as category, SUM(t.amount) as amount " +
                        "FROM transactions t " +
                        "JOIN users u ON t.user_id = u.id " +
                        "JOIN categories c ON t.category_id = c.id " +
                        "WHERE u.username = :username " +
                        "AND EXTRACT(MONTH FROM t.created_at) = :month " +
                        "AND EXTRACT(YEAR FROM t.created_at) = :year " +
                        "GROUP BY c.name", nativeQuery = true)
        List<CategorySumProjection> getRawSummary(@Param("username") String username,
                        @Param("month") int month,
                        @Param("year") int year);

        default MonthlySummary calculateSummary(String username, int month, int year) {
                List<CategorySumProjection> results = getRawSummary(username, month, year);

                BigDecimal total = results.stream()
                                .map(CategorySumProjection::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, BigDecimal> breakdown = results.stream()
                                .collect(Collectors.toMap(
                                                CategorySumProjection::getCategory,
                                                CategorySumProjection::getAmount,
                                                (existing, replacement) -> existing)); // Tránh lỗi duplicate key

                return new MonthlySummary(total, breakdown);
        }

        @QueryHints(value = {
                        @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
                        @QueryHint(name = "org.hibernate.readOnly", value = "true")
        })
        @Query("select t from Transaction t")
        Stream<Transaction> streamAllTransactions();

        // FIX 2: Sửa t.owner thành t.user cho khớp với Entity Transaction mới
        @QueryHints(value = {
                        @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
                        @QueryHint(name = "org.hibernate.readOnly", value = "true")
        })
        @Query("SELECT t FROM Transaction t WHERE t.user = :owner AND t.createdAt >= :startDate")
        Stream<Transaction> streamTransactionsFromDate(@Param("owner") User owner,
                        @Param("startDate") LocalDateTime startDate);

        // FIX 3: Kiểm tra lại sumAmountByPeriod (đảm bảo t.user)
        @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :owner AND t.createdAt BETWEEN :start AND :end")
        BigDecimal sumAmountByPeriod(@Param("owner") User owner,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);
}