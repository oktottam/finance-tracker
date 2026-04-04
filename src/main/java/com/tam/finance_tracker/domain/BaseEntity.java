package com.tam.finance_tracker.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass // Thông báo cho JPA rằng đây là một lớp cơ sở mà các thực thể khác sẽ kế thừa
@EntityListeners(AuditingEntityListener.class) // Kích hoạt tính năng tự động cập nhật createdAt và updatedAt
@Getter @Setter // Sử dụng Lombok để tự động tạo getter và setter
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
