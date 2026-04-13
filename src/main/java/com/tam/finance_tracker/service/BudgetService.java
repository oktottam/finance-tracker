package com.tam.finance_tracker.service;

import org.springframework.stereotype.Service;

import com.tam.finance_tracker.domain.Budget;
import com.tam.finance_tracker.domain.BudgetApproval;
import com.tam.finance_tracker.domain.BudgetStatus;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.dto.ApprovalRequest;
import com.tam.finance_tracker.repository.ApprovalRepository;
import com.tam.finance_tracker.repository.BudgetRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final ApprovalRepository approvalRepository;

    @Transactional
    public void approveBudget(ApprovalRequest request, User currentApprover) {
        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ngân sách"));

        // 1. Kiểm tra trùng lặp (Tâm đã duyệt chưa?)
        if (approvalRepository.existsByBudgetAndApprover(budget, currentApprover)) {
            throw new IllegalStateException("Tâm đã duyệt mục này rồi!");
        }

        // 2. Lưu phiếu duyệt
        BudgetApproval approval = new BudgetApproval();
        approval.setBudget(budget);
        approval.setApprover(currentApprover);
        approval.setStatus(BudgetStatus.APPROVED);
        approvalRepository.save(approval);

        // 3. Đếm số phiếu hiện tại
        long currentVotes = approvalRepository.countByBudgetAndStatus(budget, BudgetStatus.APPROVED);

        // 4. So sánh với ngưỡng động của chính Budget đó
        if (currentVotes >= budget.getRequiredApprovals()) {
            budget.setStatus(BudgetStatus.APPROVED);
            budgetRepository.save(budget);
            // Gửi thông báo: "Đã đủ " + budget.getRequiredApprovals() + " người duyệt!"
        }
    }
}
