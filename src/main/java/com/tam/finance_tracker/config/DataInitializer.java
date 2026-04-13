package com.tam.finance_tracker.config;

import com.tam.finance_tracker.domain.*;
import com.tam.finance_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component // Dùng Component để Spring tự quét
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Nếu đã có User thì không làm gì cả để tránh duplicate data mỗi lần restart
        if (userRepository.count() > 0) {
            log.info(">>> [SEEDER] Data đã tồn tại, bỏ qua bước khởi tạo.");
            return;
        }

        // 1. Tạo tài khoản Admin hệ thống
        createAdminUser();

        // 2. Tạo tài khoản dev và bơm dữ liệu test
        createDevData();
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(Role.ROLE_ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
        log.info(">>> [SEEDER] Đã tạo tài khoản Admin: admin/123456");
    }

    private void createDevData() {
        // Tạo User tam_dev
        User user = new User();
        user.setUsername("tam_dev");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);
        user = userRepository.save(user);

        // Tạo Categories
        Category c1 = saveCategory("Ăn uống", "🍲", TransactionType.EXPENSE);
        Category c2 = saveCategory("Di chuyển", "🏍️", TransactionType.EXPENSE);
        Category c3 = saveCategory("Tiền lương", "💵", TransactionType.INCOME);
        Category c4 = saveCategory("Mua sắm", "🛒", TransactionType.EXPENSE);

        List<Category> expenses = List.of(c1, c2, c4);
        Random random = new Random();

        // Bơm 50 giao dịch chi tiêu ngẫu nhiên
        for (int i = 0; i < 50; i++) {
            Transaction t = new Transaction();
            t.setAmount(BigDecimal.valueOf(20000 + random.nextInt(480000)));
            t.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60)));
            t.setUser(user);
            t.setCategory(expenses.get(random.nextInt(expenses.size())));
            t.setDescription("Giao dịch mẫu #" + (i + 1));
            transactionRepository.save(t);
        }

        log.info(">>> [SEEDER] Đã bơm 50 transactions cho user: tam_dev");
    }

    private Category saveCategory(String name, String icon, TransactionType type) {
        Category c = new Category();
        c.setName(name);
        c.setIcon(icon);
        c.setType(type);
        return categoryRepository.save(c);
    }
}