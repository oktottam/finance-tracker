package com.tam.finance_tracker.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tam.finance_tracker.dto.ReportDTO;
import com.tam.finance_tracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service // <-- Đảm bảo có dòng này
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepo;
    private final EsSearchService esSearchService;

    @Qualifier("reportExecutor")
    private final Executor reportExecutor;

    @Override
    @Async("reportExecutor")
    public CompletableFuture<ReportDTO> generateMonthlyReport(String username, LocalDate date) {
        var dbTask = CompletableFuture.supplyAsync(() -> 
            transactionRepo.calculateSummary(username, date.getMonthValue(), date.getYear()), reportExecutor);

        var esTask = CompletableFuture.supplyAsync(() -> 
            esSearchService.getUnusualActivities(username, date), reportExecutor)
            .exceptionally(ex -> List.of("Dữ liệu ES tạm thời không khả dụng."));

        return dbTask.thenCombine(esTask, (dbData, esData) -> new ReportDTO(
                username, date, dbData.getTotalAmount(), dbData.getCategoryBreakdown(), esData));
    }

    @Override
    public InputStream generatePieChart(Map<String, BigDecimal> breakdown) throws Exception {
        log.info(">>> [CHART] Đang vẽ biểu đồ chi tiêu...");

        // 1. Khởi tạo biểu đồ tròn đơn giản
        PieChart chart = new PieChartBuilder()
                .width(800)
                .height(600)
                .title("Cơ cấu chi tiêu của Tâm (" + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear() + ")")
                .build();

        // 2. Chỉ giữ lại các style cơ bản không gây lỗi
        chart.getStyler().setPlotContentSize(.8);

        // 3. Đổ dữ liệu
        if (breakdown == null || breakdown.isEmpty()) {
            chart.addSeries("Chưa có dữ liệu", 1);
        } else {
            breakdown.forEach(chart::addSeries);
        }

        // 4. Export ra Stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, os, BitmapFormat.PNG);
        return new ByteArrayInputStream(os.toByteArray());
    }
}