package com.tam.finance_tracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // Một User có một Role (hoặc dùng Set<Role> nếu muốn đa quyền)

    // QUAN TRỌNG: Lưu ID chat Telegram để gửi thông báo/file
    @Column(name = "telegram_chat_id", unique = true)
    private String telegramChatId;

    // Tùy chọn: Trạng thái tài khoản
    private boolean active = true;

    @Column(name = "verification_token", unique = true)
    private String verificationToken; // Mã tạm thời để link tài khoản
}
