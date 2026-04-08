package com.tam.finance_tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data // Dùng Lombok để có Getter/Setter
public class JwtProperties {
    private String secret;
    private Long expirationMs;
}
