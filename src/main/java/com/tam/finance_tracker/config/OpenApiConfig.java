package com.tam.finance_tracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Tên này phải khớp ở cả bước 1 và bước 2
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Tracker API")
                        .version("1.0")
                        .description("Tài liệu API hệ thống quản lý tài chính cá nhân"))
                // 1. Chỉ định rằng mặc định tất cả API đều cần Security này
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 2. Định nghĩa chi tiết Security Scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}