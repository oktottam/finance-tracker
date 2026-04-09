package com.tam.finance_tracker.controller;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.domain.ExportTask;
import com.tam.finance_tracker.repository.ExportTaskRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export") // Tiền tố chung cho các API liên quan đến Export
@RequiredArgsConstructor
public class ExportTaskController {
    private final ExportTaskRepository taskRepo;

    // Đây chính là đoạn code Tâm đang hỏi
    @GetMapping("/status/{taskId}")
    public ResponseEntity<ExportTask> getTaskStatus(@PathVariable String taskId) {
        return taskRepo.findById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{taskId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String taskId) {
        // 1. Tìm thông tin task trong DB để lấy tên file/đường dẫn
        return taskRepo.findById(taskId).<ResponseEntity<Resource>>map(task -> {
            // 2. Xác định file vật lý
            File file = new File("exports/report_" + taskId + ".csv");

            if (!file.exists()) {
                // Cần giữ <Resource> ở đây để đồng nhất kiểu trả về
                return ResponseEntity.notFound().<Resource>build();
            }

            Resource resource = new FileSystemResource(file);

            // 3. Thiết lập Header và trả về file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        }).orElse(ResponseEntity.notFound().build());
    }
}
