package com.tam.finance_tracker.repository;

import com.tam.finance_tracker.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Tìm danh mục theo tên (ví dụ: để kiểm tra trùng lặp khi tạo mới)
    Optional<Category> findByName(String name);
    
    // Kiểm tra tồn tại theo tên
    boolean existsByName(String name);
}