package com.tam.finance_tracker.service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import com.tam.finance_tracker.dto.ReportDTO;

public interface ReportService {
    // Trả về CompletableFuture để Controller không bị "treo" khi đợi dữ liệu
    CompletableFuture<ReportDTO> generateMonthlyReport(String username, LocalDate date);
}
