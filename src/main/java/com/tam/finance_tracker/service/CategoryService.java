package com.tam.finance_tracker.service;

import java.util.Optional;
import com.tam.finance_tracker.domain.Category;
import com.tam.finance_tracker.domain.User;

public interface CategoryService {
    
    // 1. Tìm kiếm mặc định (Dùng cho Admin hoặc hệ thống chung)
    Optional<Category> findByKeyword(String keyword);

    // 2. Tìm kiếm cá nhân hóa (Ưu tiên thói quen của từng User)
    // Bot sẽ tìm trong 'từ điển riêng' trước, nếu không có mới tìm 'từ điển chung'
    Optional<Category> findByKeywordForUser(String keyword, User user);

    // 3. Cơ chế "Dạy" Bot (Mapping từ khóa lạ vào danh mục cho User cụ thể)
    // Dùng khi User bấm xác nhận danh mục trên Telegram
    void learnNewMapping(User user, Category category, String rawKeyword);

    // 4. Quản lý danh mục
    Category save(Category category);
    
    Optional<Category> findById(Long id);
}