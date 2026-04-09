package com.tam.finance_tracker.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tam.finance_tracker.security.JwtAuthFilter;
import com.tam.finance_tracker.security.RateLimitFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity // Kích hoạt Spring Security trong ứng dụng
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter; // Tiêm người gác cổng vào
    private final RateLimitFilter rateLimitFilter; // Tiêm bộ lọc giới hạn tốc độ vào
    private final UserDetailsService userDetailsService; // Tiêm UserDetailsService để lấy thông tin người dùng từ DB

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORS & CSRF
            .cors(Customizer.withDefaults()) // Rất quan trọng để gọi API từ Angular
            .csrf(csrf -> csrf.disable()) // Tắt CSRF vì chúng ta dùng JWT, không cần bảo vệ CSRF
            
            // 2. Chế độ không lưu Session (Stateless) - Dùng cho JWT   
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 3. Gác cửa Rate Limit
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

            // 4. Phân quyền
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Mở cửa cho API đăng ký/đăng nhập
                .requestMatchers("/api/auth/**").permitAll() 
                
                .anyRequest().authenticated()
            )
            
            // 5. JWT Filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 1. Chỉ cho phép các domain này gọi tới (Angular Port 4200)
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); 
        
        // 2. Cho phép các phương thức HTTP nào
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 3. Cho phép các Header nào (JWT thường nằm trong Authorization)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        
        // 4. Cho phép gửi Credentials (nếu Tâm dùng Cookie hoặc Auth Header)
        configuration.setAllowCredentials(true);
        
        // 5. Áp dụng cấu hình này cho tất cả các đường dẫn (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
