package com.tam.finance_tracker.scheduler;

import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.repository.UserRepository;
import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.service.AsyncExportService;
import com.tam.finance_tracker.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportScheduler {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepo;
    private final AsyncExportService exportService;
    private final TelegramBotService botService;

    // Chạy vào 8h00 sáng Thứ Hai hàng tuần
    @Scheduled(cron = "0 0 8 * * MON")
    public void runWeeklyJob() {
        log.info("🔔 [CRON] Bắt đầu tiến trình Báo cáo tuần tự động...");

        LocalDateTime now = LocalDateTime.now();
        // Lấy 00:00:00 của Thứ Hai tuần trước
        LocalDateTime lastMonday = now.minusDays(7).withHour(0).withMinute(0).withSecond(0);
        // Lấy 00:00:00 của Thứ Hai tuần trước nữa (để so sánh)
        LocalDateTime previousMonday = lastMonday.minusDays(7);

        userRepository.findAllByTelegramChatIdIsNotNull().forEach(user -> {
            try {
                // 1. Lấy tổng chi tiêu tuần này và tuần trước
                BigDecimal currentSum = transactionRepo.sumAmountByPeriod(user, lastMonday, now);
                BigDecimal previousSum = transactionRepo.sumAmountByPeriod(user, previousMonday, lastMonday);

                // Tránh null pointer nếu tuần đó không tiêu gì
                currentSum = (currentSum != null) ? currentSum : BigDecimal.ZERO;
                previousSum = (previousSum != null) ? previousSum : BigDecimal.ZERO;

                // 2. Tính xu hướng (🔺/🔻)
                String trend = calculateTrend(currentSum, previousSum);

                // 3. Gửi tin nhắn Dashboard
                String msg = String.format(
                        "📊 *BÁO CÁO CHI TIÊU TUẦN QUA*\n\n" +
                                "💰 Tổng chi: %,.0f VNĐ\n" +
                                "📈 So với tuần trước: %s\n\n" +
                                "📎 Đang khởi tạo file chi tiết gửi Tâm ngay đây...",
                        currentSum, trend);
                botService.sendMessage(user.getTelegramChatId(), msg, null);

                // 4. Trigger Export Task (Tự động sinh file CSV gửi cho Tâm)
                String taskId = UUID.randomUUID().toString();
                exportService.processExport(taskId);

            } catch (Exception e) {
                log.error("Lỗi gửi báo cáo tuần cho {}: {}", user.getUsername(), e.getMessage());
            }
        });
    }

    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0)
            return "Mới (Chưa có dữ liệu tuần trước)";

        BigDecimal diff = current.subtract(previous);
        double percent = (diff.doubleValue() / previous.doubleValue()) * 100;

        String icon = (percent >= 0) ? "🔺 Tăng" : "🔻 Giảm";
        return String.format("%s %.1f%%", icon, Math.abs(percent));
    }
}