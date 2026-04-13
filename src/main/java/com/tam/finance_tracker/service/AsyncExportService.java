package com.tam.finance_tracker.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;
import com.tam.finance_tracker.domain.ExportTask;
import com.tam.finance_tracker.domain.TaskStatus;
import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.repository.ExportTaskRepository;
import com.tam.finance_tracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncExportService {
    private final TransactionRepository transactionRepo;
    private final ExportTaskRepository taskRepo; // 1. Tiêm "Sổ hộ khẩu" vào đây
    private final TelegramBotService botService; // 🚀 Tiêm "linh hồn" của Bot vào đây

    @Async("exportTaskExecutor")
    @Transactional(readOnly = true) // BẮT BUỘC để duy trì Connection cho Stream
    public void processExport(String taskId) { // Trả về void vì đã có DB theo dõi
        // 1. Tìm Task
        ExportTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Task ID: " + taskId));

        // 2. Lấy User để có chatId (Giả sử task có chứa ownerId hoặc liên kết User)
        User user = task.getOwner();
        if (user == null || user.getTelegramChatId() == null) {
            log.error("Task {} không có thông tin Telegram người dùng!", taskId);
            return;
        }

        String userChatId = user.getTelegramChatId();

        // BÁO BẮT ĐẦU
        updateTaskStatus(taskId, TaskStatus.PROCESSING, 0, null);
        botService.sendMessage(userChatId, "🚀 [START] Bắt đầu trích xuất dữ liệu cho Tâm...", null);

        long total = transactionRepo.count();
        if (total == 0) {
            handleEmptyData(taskId, userChatId);
            return;
        }

        String filePath = "exports/report_" + taskId + ".csv";
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                CSVWriter csvWriter = new CSVWriter(osw);
                Stream<Transaction> transactionStream = transactionRepo.streamAllTransactions()) {

            osw.write('\uFEFF'); // BOM cho Excel
            csvWriter.writeNext(new String[] { "ID", "Số tiền", "Nội dung", "Ngày tạo" });

            // Sử dụng AtomicInteger vì biến trong lambda phải là final hoặc effectively
            // final
            AtomicInteger count = new AtomicInteger(0);

            transactionStream.forEach(t -> {
                csvWriter.writeNext(new String[] {
                        t.getId().toString(),
                        t.getAmount().toString(),
                        t.getDescription(),
                        t.getCreatedAt().toString()
                });

                int currentCount = count.incrementAndGet();
                updateProgress(taskId, userChatId, currentCount, total);
            });

            updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" + taskId);
            botService.sendMessage(userChatId, "✅ [SUCCESS] File của Tâm đã sẵn sàng!", null);

        } catch (Exception e) {
            updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
            botService.sendMessage(userChatId, "❌ [FAILED] Có lỗi rồi Tâm ơi: " + e.getMessage(), null);
        }
    }

    private void updateProgress(String taskId, String chatId, int currentCount, long total) {
        int currentProgress = (int) (((double) currentCount / total) * 100);
        if (currentProgress % 10 == 0 || currentCount == total) {
            updateTaskStatus(taskId, TaskStatus.PROCESSING, currentProgress, null);

            // Chỉ báo qua Bot mỗi 20% để tránh bị Telegram đánh "spam" (Rate limit)
            if (currentProgress % 20 == 0) {
                botService.sendMessage(chatId, "📊 Tiến độ: " + currentProgress + "%", null);
            }
        }
    }

    private void updateTaskStatus(String taskId, TaskStatus status, int progress, String url) {
        taskRepo.findById(taskId).ifPresent(task -> {
            task.setStatus(status);
            task.setProgress(progress); // Đảm bảo Entity ExportTask đã có field này
            if (url != null)
                task.setDownloadUrl(url);
            taskRepo.save(task);
        });
    }

    private void handleEmptyData(String taskId, String chatId) {
        log.warn("Task {}: Không tìm thấy dữ liệu giao dịch cho user {}", taskId, chatId);

        // 1. Cập nhật DB
        updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, null);

        // 2. Báo đúng người, đúng việc
        String alertMsg = "⚠️ *Thông báo*: Task #" + taskId
                + "\nKhông có dữ liệu giao dịch trong khoảng thời gian này. Tâm kiểm tra lại nhé!";
        botService.sendMessage(chatId, alertMsg, null);
    }
}
