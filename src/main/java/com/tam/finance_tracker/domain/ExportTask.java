package com.tam.finance_tracker.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ExportTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskId; // Dùng UUID.randomUUID()
    
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    private String downloadUrl; // Đường dẫn để tải file sau khi xong
    private LocalDateTime createdAt;
    @Column(name = "progress")
    private int progress = 0; // Khởi tạo bằng 0;
}
