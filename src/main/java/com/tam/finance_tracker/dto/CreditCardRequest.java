package com.tam.finance_tracker.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data // Tự động tạo getter, setter, toString, equals, hashCode
public class CreditCardRequest {
    @NotBlank(message = "Tên thẻ không được để trống")
    private String cardName;

    @NotNull
    @DecimalMin(value = "0", message = "Hạn mức không được âm")
    private BigDecimal limitAmount;

    @Min(1) @Max(31)
    private Integer statementDay;

    @Min(0)
    private Integer dueDateOffset;
}
