package com.tam.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.domain.BudgetApproval;
import com.tam.finance_tracker.domain.BudgetStatus;
import com.tam.finance_tracker.domain.User;

@Repository
public interface ApprovalRepository extends JpaRepository<BudgetApproval, Long> {
    
    // Kiểm tra xem Admin này đã từng duyệt Budget này chưa
    boolean existsByBudgetAndApprover(Budget budget, User approver);

    // Đếm số lượng phiếu APPROVED cho một ngân sách cụ thể
    long countByBudgetAndStatus(Budget budget, BudgetStatus status);
}
