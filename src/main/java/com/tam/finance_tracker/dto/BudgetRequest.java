package com.tam.finance_tracker.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetRequest {
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Số tiền hạn mức không được để trống")
    @DecimalMin(value = "0.0", message = "Hạn mức phải lớn hơn hoặc bằng 0")
    private BigDecimal limitAmount;

    @Min(1) @Max(12)
    private int month;

    @Min(2026) // Giả định hệ thống bắt đầu từ năm nay
    private int year;
}
