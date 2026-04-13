package com.tam.finance_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.domain.BudgetStatus;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Lấy kèm Category để khi trả về Response không phải query thêm
    @EntityGraph(attributePaths = {"category"})
    Optional<Budget> findById(Long id);

    // Dùng cho ETL: Chỉ lấy các ngân sách đã được duyệt hoàn toàn
    List<Budget> findByMonthAndYearAndStatus(int month, int year, BudgetStatus status);
}
