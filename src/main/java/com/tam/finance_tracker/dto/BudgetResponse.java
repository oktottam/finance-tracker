package com.tam.finance_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private String categoryName;
    private BigDecimal limitAmount;
    private int month;
    private int year;
    private String status; // DRAFT, PENDING, APPROVED, REJECTED
    private String approvedBy; // Tên Admin/Manager đã duyệt
    private String note;
    private LocalDateTime updatedAt;
    private int currentApprovals; // Số lượng người đã duyệt
    private int requiredApprovals; // Số lượng cần thiết (ví dụ mặc định là 2)
    private List<String> approverNames; // Danh sách những người đã duyệt để Tâm biết ai chưa duyệt
}
