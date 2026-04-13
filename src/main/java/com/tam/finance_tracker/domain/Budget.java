package com.tam.finance_tracker.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Liên kết tới Category vừa tạo

    @NotNull
    private BigDecimal limitAmount; // Số tiền tối đa định tiêu

    @NotNull
    private Integer month;

    @NotNull
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "required_approvals")
    private int requiredApprovals = 2; // Giá trị mặc định, có thể thay đổi tùy lúc tạo

    @Enumerated(EnumType.STRING)
    private BudgetStatus status = BudgetStatus.DRAFT;

    private String notes; // Lý do duyệt hoặc từ chối

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Ai là người duyệt ngân sách này?
}