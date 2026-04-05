package com.tam.finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tam.finance_tracker.security.JwtAuthFilter;
import com.tam.finance_tracker.security.RateLimitFilter;

import lombok.RequiredArgsConstructor;

@Configuration 
@EnableWebSecurity // Kích hoạt Spring Security trong ứng dụng
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter; // Tiêm người gác cổng vào
    private final RateLimitFilter rateLimitFilter; // Tiêm bộ lọc giới hạn tốc độ vào

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
  
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF (Cần thiết để test các API POST/PUT từ Swagger/Postman)
            .csrf(csrf -> csrf.disable()) 
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Gác cửa đầu tiên để kiểm tra giới hạn tốc độ trước khi đi vào phần xác thực
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
            
            // // 3. Sử dụng cấu hình mặc định cho Form Login và Basic Auth
            // .formLogin(Customizer.withDefaults())
            // .httpBasic(Customizer.withDefaults());

            // Thêm dòng này để chèn JwtAuthFilter vào trước filter mặc định của Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
