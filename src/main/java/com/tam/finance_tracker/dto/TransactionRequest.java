package com.tam.finance_tracker.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền giao dịch phải lớn hơn 0") // Chỉnh lại 0.01 cho linh hoạt
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId; // Dùng ID thay vì nguyên Object Entity

    @NotNull(message = "Thẻ không được để trống")
    private Long cardId;
}