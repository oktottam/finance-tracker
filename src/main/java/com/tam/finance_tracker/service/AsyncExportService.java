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
import com.tam.finance_tracker.domain.TaskStatus;
import com.tam.finance_tracker.domain.Transaction;
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
        // BƯỚC 1: Khởi động - Báo cho DB và báo cho Tâm qua Telegram
        updateTaskStatus(taskId, TaskStatus.PROCESSING, 0, null);
        botService.sendMessage("🚀 [START] Bắt đầu Stream dữ liệu cho Task: " + taskId);

        long total = transactionRepo.count();
        if (total == 0) {
            handleEmptyData(taskId);
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
                updateProgress(taskId, currentCount, total);
            });

            updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" + taskId);
            botService.sendMessage("✅ [SUCCESS] Task " + taskId + " hoàn thành! RAM vẫn cực kỳ thảnh thơi.");

        } catch (Exception e) {
            updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
            botService.sendMessage("❌ [FAILED] Task " + taskId + " lỗi: " + e.getMessage());
            log.error("Export Error: ", e);
        }
    }

    private void updateProgress(String taskId, int currentCount, long total) {
        int currentProgress = (int) (((double) currentCount / total) * 100);
        // Chỉ cập nhật DB và báo Bot mỗi khi tăng thêm 10% để tránh nghẽn mạng/DB
        if (currentProgress % 10 == 0 || currentCount == total) {
            updateTaskStatus(taskId, TaskStatus.PROCESSING, currentProgress, null);
            if (currentProgress % 20 == 0) {
                botService.sendMessage("📊 Task " + taskId + " progress: " + currentProgress + "%");
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

    private void handleEmptyData(String taskId) {
        log.warn("Task {}: Không tìm thấy dữ liệu giao dịch nào để xuất file.", taskId);

        // 1. Cập nhật trạng thái COMPLETED nhưng tiến độ là 0 hoặc 100 tùy Tâm quy định
        // Ở đây mình để 100 và URL là null để Angular biết là xong nhưng không có file
        // tải
        updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, null);

        // 2. Báo cho "Bot giám sát" để Tâm biết ngay lập tức
        botService.sendMessage("⚠️ [EMPTY] Task " + taskId
                + ": Không có dữ liệu giao dịch trong khoảng thời gian này. Hệ thống đã dừng xuất file.");
    }
}
