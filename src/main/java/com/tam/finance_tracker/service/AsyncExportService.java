package com.tam.finance_tracker.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

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

    @Async("exportTaskExecutor")
    public void processExport(String taskId) { // Trả về void vì đã có DB theo dõi
        // // BƯỚC 1: Khởi động - 0%
        updateTaskStatus(taskId, TaskStatus.PROCESSING, 0, null);

        // try {
        // // Lấy dữ liệu và tính tổng số dòng để làm mẫu số
        // var allTransactions = transactionRepo.findAll();
        // int total = allTransactions.size();

        // String filePath = "exports/report_" + taskId + ".csv";
        // File file = new File(filePath);
        // file.getParentFile().mkdirs();

        // try (PrintWriter writer = new PrintWriter(file)) {
        // writer.println("ID,Amount,Description,Date");

        // // BƯỚC 2: Thay forEach bằng vòng lặp for để lấy chỉ số (index)
        // for (int i = 0; i < total; i++) {
        // var t = allTransactions.get(i);
        // writer.println(String.format("%s,%s,%s,%s",
        // t.getId(), t.getAmount(), t.getDescription(), t.getCreatedAt()));

        // // Tính % tiến độ: (vị trí hiện tại / tổng số) * 100
        // int currentProgress = (int) (((double) (i + 1) / total) * 100);

        // // Tối ưu: Chỉ update DB khi tiến độ nhảy thêm 10% hoặc là dòng cuối cùng
        // if (currentProgress % 10 == 0 || i == total - 1) {
        // updateTaskStatus(taskId, TaskStatus.PROCESSING, currentProgress, null);
        // }
        // }
        // }

        // // BƯỚC 3: Hoàn thành - 100% kèm Link download
        // updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" +
        // taskId);
        // log.info("Task {} hoàn thành rực rỡ!", taskId);

        // } catch (Exception e) {
        // updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
        // log.error("Task {} thất bại: {}", taskId, e.getMessage());
        // }

        // try {
        // var allTransactions = transactionRepo.findAll();
        // int total = allTransactions.size();
        // String filePath = "exports/report_" + taskId + ".csv";
        // File file = new File(filePath);
        // file.getParentFile().mkdirs();

        // // Dùng I/O chuẩn: FileWriter -> CSVWriter
        // try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
        // // 1. Ghi Header
        // String[] header = {"ID", "Amount", "Description", "Date"};
        // csvWriter.writeNext(header);

        // // 2. Ghi từng dòng và tính toán Progress
        // for (int i = 0; i < total; i++) {
        // var t = allTransactions.get(i);

        // // Chuyển object thành mảng String - OpenCSV sẽ lo phần format
        // String[] data = {
        // t.getId().toString(),
        // t.getAmount().toString(),
        // t.getDescription(),
        // t.getCreatedAt().toString()
        // };
        // csvWriter.writeNext(data);

        // // Cập nhật % tiến độ (Chỉ lưu vào DB mỗi 10% để tối ưu)
        // int currentProgress = (int) (((double) (i + 1) / total) * 100);
        // if (currentProgress % 10 == 0 || i == total - 1) {
        // updateTaskStatus(taskId, TaskStatus.PROCESSING, currentProgress, null);
        // }
        // }
        // } // CSVWriter tự động flush và close ở đây nhờ try-with-resources

        // updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" +
        // taskId);
        // log.info("Task {} hoàn thành rực rỡ với OpenCSV!", taskId);

        // } catch (Exception e) {
        // updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
        // log.error("Lỗi I/O khi dùng OpenCSV cho task {}: {}", taskId,
        // e.getMessage());
        // }

        try {
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
                    }
                }
            }

            updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "/api/export/download/" + taskId);
            log.info("Task {} hoàn thành rực rỡ, tiếng Việt xanh mượt!", taskId);

        } catch (Exception e) {
            updateTaskStatus(taskId, TaskStatus.FAILED, 0, null);
            log.error("Lỗi xuất file cho Tâm: {}", e.getMessage());
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
