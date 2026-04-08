package com.tam.finance_tracker.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlySummary(
    BigDecimal totalSpending,
    Map<String, BigDecimal> categoryBreakdown
) {}
