package com.tam.finance_tracker.dto;

import java.math.BigDecimal;

import com.tam.finance_tracker.domain.TransactionCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.1", message = "Số tiền giao dịch phải lớn hơn 0")
    private BigDecimal amount;

    private String description;

    @NotNull
    private TransactionCategory category;

    @NotNull
    private Long cardId;
}
