package com.tam.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.ExportTask;

@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, String> {
    // Không cần viết gì thêm, Tâm đã có đủ các hàm save, findById, delete...
}
