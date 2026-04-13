package com.tam.finance_tracker.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummary {
    private BigDecimal totalAmount; // Đổi 'total' thành 'totalAmount'
    private Map<String, BigDecimal> categoryBreakdown; // Đổi 'breakdown' thành 'categoryBreakdown'
}