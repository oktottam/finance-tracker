package com.tam.finance_tracker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name; // Ăn uống, Di chuyển...
    private String icon; // Lưu emoji để hiển thị lên Telegram cho đẹp
    
    @Enumerated(EnumType.STRING)
    private TransactionType type; // Loại mặc định cho category này (INCOME/EXPENSE)
}