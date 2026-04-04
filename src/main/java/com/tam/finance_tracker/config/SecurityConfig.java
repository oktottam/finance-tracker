package com.tam.finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration 
@EnableWebSecurity // Kích hoạt Spring Security trong ứng dụng
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF (Cần thiết để test các API POST/PUT từ Swagger/Postman)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Cấu hình phân quyền đường dẫn
            .authorizeHttpRequests(auth -> auth
                // Cho phép tất cả mọi người truy cập Swagger và tài liệu API
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Tạm thời cho phép truy cập các API nghiệp vụ để bạn test nhanh
                .requestMatchers("/api/**").permitAll()
                
                // Các yêu cầu còn lại bắt buộc phải xác thực (nếu chưa mở hết)
                .anyRequest().authenticated()
            )
            
            // 3. Sử dụng cấu hình mặc định cho Form Login và Basic Auth
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
