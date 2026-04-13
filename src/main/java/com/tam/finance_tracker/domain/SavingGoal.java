package com.tam.finance_tracker.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "saving_goals")
@Getter @Setter
public class SavingGoal extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String goalName; // Ví dụ: "Mua phuộc Nitron"
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime deadline;
}