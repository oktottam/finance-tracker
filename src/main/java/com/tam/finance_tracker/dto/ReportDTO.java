package com.tam.finance_tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReportDTO (
    String username,
    LocalDate reportDate,
    BigDecimal totalSpending,
    Map<String, BigDecimal> categorySummary,
    List<String> unusualActivities // Dữ liệu từ Elasticsearch
){}
