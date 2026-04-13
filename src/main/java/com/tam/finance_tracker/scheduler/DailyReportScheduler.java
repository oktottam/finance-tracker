package com.tam.finance_tracker.scheduler;

import java.math.BigDecimal;
import java.io.InputStream;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.repository.UserRepository;
import com.tam.finance_tracker.service.TelegramBotService;
import com.tam.finance_tracker.service.ReportService; // Thêm import này

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class DailyReportScheduler {

    private final TransactionRepository transactionRepository;
    private final TelegramBotService telegramBotService;
    private final UserRepository userRepository;
    private final ReportService reportService; // Inject ReportService để vẽ hình

    @Scheduled(fixedRate = 60000)
    public void sendTestReport() {
        userRepository.findByUsername("tam_dev").ifPresent(user -> {
            if (user.getTelegramChatId() != null) {
                try {
                    int month = java.time.LocalDate.now().getMonthValue();
                    int year = java.time.LocalDate.now().getYear();

                    var summary = transactionRepository.calculateSummary(user.getUsername(), month, year);
                    BigDecimal foodAmount = summary.getCategoryBreakdown().getOrDefault("Ăn uống", BigDecimal.ZERO);

                    // 1. Tạo nội dung chữ (Caption)
                    String caption = String.format(
                            "📊 *BÁO CÁO CHI TIÊU THÁNG %d/%d*\n" +
                            "--------------------------\n" +
                            "💰 Tổng chi tiêu: *%,.0f VNĐ*\n" +
                            "🍲 Ăn uống: *%,.0f VNĐ*\n" +
                            "--------------------------\n" +
                            "🚀 _Biểu đồ được vẽ từ Backend của Tâm!_",
                            month, year,
                            summary.getTotalAmount(),
                            foodAmount);

                    // 2. Gọi ReportService để vẽ hình Pie Chart
                    InputStream chartImage = reportService.generatePieChart(summary.getCategoryBreakdown());

                    // 3. Gửi ẢNH kèm Caption (Thay vì gửi tin nhắn text đơn thuần)
                    telegramBotService.sendPhoto(
                        user.getTelegramChatId(), 
                        chartImage, 
                        "report.png", 
                        caption
                    );
                    
                    log.info(">>> [SCHEDULER] Đã bắn báo cáo hình ảnh cho Tâm thành công!");

                } catch (Exception e) {
                    log.error("Lỗi khi tạo báo cáo hình ảnh: {}", e.getMessage());
                    // Nếu lỗi vẽ hình thì gửi tin nhắn text chữa cháy
                    telegramBotService.sendSimpleMessage(user.getTelegramChatId(), "Lỗi vẽ biểu đồ, Tâm check log nhé!");
                }
            }
        });
    }
}