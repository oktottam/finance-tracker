package com.tam.finance_tracker.domain;

public enum BudgetStatus {
    DRAFT,      // Đang soạn thảo
    PENDING,    // Đang chờ duyệt
    APPROVED,   // Đã duyệt (Bắt đầu áp dụng để so sánh với Transaction)
    REJECTED    // Bị từ chối
}
