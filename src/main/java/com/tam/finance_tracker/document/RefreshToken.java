package com.tam.finance_tracker.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("refresh_tokens") // Lưu vào Redis cho nhanh
public class RefreshToken {
    @Id
    private String token;
    private String username;
    private Instant expiryDate;
}
