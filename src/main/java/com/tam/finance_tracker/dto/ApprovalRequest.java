package com.tam.finance_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {
    @NotNull(message = "ID ngân sách là bắt buộc")
    private Long budgetId;

    @NotBlank(message = "Trạng thái duyệt không được để trống")
    private String status; // APPROVED hoặc REJECTED

    private String note; // Lý do duyệt hoặc từ chối (nếu có)
}