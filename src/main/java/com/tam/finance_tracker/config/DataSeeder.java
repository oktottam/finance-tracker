package com.tam.finance_tracker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tam.finance_tracker.domain.Role;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            // Nhớ là password phải được mã hóa trước khi lưu nhé Tâm!
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ROLE_ADMIN);
            
            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản Admin mặc định: admin / 123456");
        }
    }
}
