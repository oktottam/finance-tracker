package com.tam.finance_tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tam.finance_tracker.dto.ReportDTO;
import com.tam.finance_tracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final TransactionRepository transactionRepo;
    private final EsSearchService esSearchService;

    @Override
    @Async("reportExecutor") // Dùng cái Executor Tâm đã cấu hình ở AsyncConfig
    public CompletableFuture<ReportDTO> generateMonthlyReport(String username, LocalDate date) {
        log.info("Bắt đầu tổng hợp báo cáo cho Tâm tại luồng: {}", Thread.currentThread().getName());

        // Luồng 1: Lấy tổng chi tiêu từ Postgres (SQL)
        var dbTask = CompletableFuture.supplyAsync(() -> {
            log.info("Đang truy vấn Postgres...");
            return transactionRepo.calculateSummary(username, date.getMonthValue(), date.getYear());
        });

        // Luồng 2: Lấy các hoạt động bất thường từ Elasticsearch (NoSQL)
        var esTask = CompletableFuture.supplyAsync(() -> {
            log.info("Đang truy vấn Elasticsearch...");
            return esSearchService.getUnusualActivities(username, date);
        }).exceptionally(ex -> {
            log.error("ES gặp sự cố, trả về danh sách rỗng để báo cáo vẫn chạy tiếp!");
            return List.of("Không thể kết nối ES, Tâm kiểm tra lại Docker nhé");
        });

        // Gộp kết quả của 2 luồng (thenCombine)
        return dbTask.thenCombine(esTask, (dbData, esData) -> {
            log.info("Gộp dữ liệu thành công!");
            return new ReportDTO(
                    username,
                    date,
                    dbData.totalSpending(),
                    dbData.categoryBreakdown(),
                    esData);
        });
    }
}
