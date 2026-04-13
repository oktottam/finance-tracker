package com.tam.finance_tracker.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.tam.finance_tracker.dto.ReportDTO;

public interface ReportService {
    // Trả về CompletableFuture để Controller không bị "treo" khi đợi dữ liệu
    CompletableFuture<ReportDTO> generateMonthlyReport(String username, LocalDate date);

    // Khai báo method để các bên khác (như Scheduler) có thể dùng
    InputStream generatePieChart(Map<String, BigDecimal> breakdown) throws Exception;
}
