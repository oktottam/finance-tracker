package com.tam.finance_tracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_category_mapping", indexes = {
        @Index(name = "idx_user_keyword", columnList = "user_id, keyword", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 1. Liên kết tới User (Để biết đây là từ điển của ai)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. Liên kết tới Category gốc (Để biết từ khóa đó trỏ về mục nào)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 3. Từ khóa cá nhân (Ví dụ: "cafe", "xang", "grab")
    @Column(nullable = false)
    private String keyword;

    // 4. Tần suất sử dụng (Tùy chọn: Để sau này Tâm gợi ý những mục hay dùng nhất)
    private Integer usageCount = 1;
}