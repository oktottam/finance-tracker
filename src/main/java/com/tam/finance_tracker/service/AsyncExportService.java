package com.tam.finance_tracker.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.tam.finance_tracker.domain.TaskStatus;
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
    public void processExport(String taskId) { // Trả về void vì đã có DB theo dõi
        // BƯỚC 1: Khởi động - Báo cho DB và báo cho Tâm qua Telegram
        updateTaskStatus(taskId, TaskStatus.PROCESSING, 0, null);
        botService.sendMessage("🚀 [START] Task " + taskId + " đã bắt đầu xử lý!");

        try {
            // Lưu ý nhỏ: findAll() sẽ ổn nếu dữ liệu ít,
            // nhưng với IQ 130 Tâm nên cân nhắc dùng Stream nếu dữ liệu lên hàng vạn dòng
            // nhé!
            var allTransactions = transactionRepo.findAll();
            int total = allTransactions.size();
            String filePath = "exports/report_" + taskId + ".csv";
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // 1. Dùng FileOutputStream để có quyền kiểm soát byte cao hơn
            try (FileOutputStream fos = new FileOutputStream(file);
                    // 2. Ép kiểu UTF-8 chuẩn xác
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    CSVWriter csvWriter = new CSVWriter(osw)) {

                // 3. Ghi ký tự BOM (\uFEFF) - Đây là "mật mã" cho Excel
                osw.write('\uFEFF');

                // 4. Ghi Header và Dữ liệu như bình thường
                csvWriter.writeNext(new String[] { "ID", "Số tiền", "Nội dung", "Ngày tạo" });

                for (int i = 0; i < total; i++) {
                    var t = allTransactions.get(i);
                    csvWriter.writeNext(new String[] {
                            t.getId().toString(),
                            t.getAmount().toString(),
                            t.getDescription(), // Tiếng Việt ở đây sẽ được bảo toàn
                            t.getCreatedAt().toString()
                    });

                    int currentProgress = (int) (((double) (i + 1) / total) * 100);
                    if (currentProgress % 10 == 0 || i == total - 1) {
                        updateTaskStatus(taskId, TaskStatus.PROCESSING, currentProgress, null);

                        // Chỉ báo Telegram ở các mốc quan trọng để tránh bị Spam
                        if (currentProgress == 50) {
                            botService.sendMessage("⏳ Task " + taskId + " đã đi được nửa chặng đường (50%).");
                        }
                    }
                }
            }

            // BƯỚC CUỐI: Thành công rực rỡ
            updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" + taskId);
            botService.sendMessage("✅ [SUCCESS] Task " + taskId + " hoàn thành 100%. Tiếng Việt xanh mượt!");

        } catch (Exception e) {
            updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
            botService.sendMessage("❌ [FAILED] Task " + taskId + " gặp sự cố: " + e.getMessage());
        }
    }

    // Hàm helper "Ghi sổ" phiên bản nâng cấp có thêm Progress
    private void updateTaskStatus(String taskId, TaskStatus status, int progress, String url) {
        taskRepo.findById(taskId).ifPresent(task -> {
            task.setStatus(status);
            task.setProgress(progress); // Đảm bảo Entity ExportTask đã có field này
            if (url != null)
                task.setDownloadUrl(url);
            taskRepo.save(task);
        });
    }
}
