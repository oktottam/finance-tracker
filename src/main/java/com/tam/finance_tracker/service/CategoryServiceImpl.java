package com.tam.finance_tracker.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.domain.Category;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.domain.UserCategoryMapping;
import com.tam.finance_tracker.repository.CategoryRepository;
import com.tam.finance_tracker.repository.UserCategoryMappingRepository;
import com.tam.finance_tracker.util.VNCharacterUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserCategoryMappingRepository mappingRepository;

    /**
     * Tìm kiếm thông minh cho đa người dùng
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByKeywordForUser(String keyword, User user) {
        String normalized = VNCharacterUtils.normalizeForSearch(keyword);
        
        // 1. Ưu tiên tìm trong "từ điển riêng" của User trước
        Optional<Category> personalCategory = mappingRepository
                .findByUserIdAndKeyword(user.getId(), normalized)
                .map(UserCategoryMapping::getCategory);

        if (personalCategory.isPresent()) {
            log.debug(">>> [MATCH] Tìm thấy mapping cá nhân cho '{}' -> {}", normalized, personalCategory.get().getName());
            return personalCategory;
        }

        // 2. Nếu không thấy, mới tìm trong "từ điển chung" (search_keywords gốc)
        return categoryRepository.findByKeyword(normalized);
    }

    /**
     * "Dạy" Bot từ khóa mới cho riêng User này
     */
    @Transactional
    public void learnNewMapping(User user, Category category, String rawKeyword) {
        String normalized = VNCharacterUtils.normalizeForSearch(rawKeyword);
        
        // Kiểm tra xem đã có mapping này chưa để tránh lỗi duplicate
        boolean exists = mappingRepository.findByUserIdAndKeyword(user.getId(), normalized).isPresent();
        
        if (!exists) {
            UserCategoryMapping mapping = new UserCategoryMapping();
            mapping.setUser(user);
            mapping.setCategory(category);
            mapping.setKeyword(normalized);
            mappingRepository.save(mapping);
            log.info(">>> [LEARNED] Bot đã nhớ: '{}' của {} là {}", normalized, user.getUsername(), category.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findByKeyword(String keyword) {
        String normalized = VNCharacterUtils.normalizeForSearch(keyword);
        return categoryRepository.findByKeyword(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category save(Category category) {
        if (category.getSearchKeywords() == null || category.getSearchKeywords().isEmpty()) {
            category.setSearchKeywords(VNCharacterUtils.normalizeForSearch(category.getName()));
        }
        return categoryRepository.save(category);
    }
}