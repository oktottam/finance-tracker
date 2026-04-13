package com.tam.finance_tracker.repository;

import com.tam.finance_tracker.domain.UserCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCategoryMappingRepository extends JpaRepository<UserCategoryMapping, String> {
    // Tìm mapping dựa trên User và Từ khóa
    Optional<UserCategoryMapping> findByUserIdAndKeyword(Long userId, String keyword);
}