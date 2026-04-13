package com.tam.finance_tracker.config;

import com.tam.finance_tracker.domain.*;
import com.tam.finance_tracker.repository.*;
import com.tam.finance_tracker.util.VNCharacterUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CreditCardRepository creditCardRepository; // Inject thêm để tạo thẻ
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Kiểm tra nếu đã có dữ liệu thì không chạy lại
        if (userRepository.count() > 0) {
            log.info(">>> [SEEDER] Dữ liệu đã tồn tại. Để reset, hãy drop table hoặc dùng create-drop.");
            return;
        }

        log.info(">>> [SEEDER] Bắt đầu khởi tạo dữ liệu mẫu...");

        // 1. Tạo tài khoản Admin
        createAdminUser();

        // 2. Tạo tài khoản dev và các quan hệ liên quan
        createDevData();
        
        log.info(">>> [SEEDER] Hoàn tất khởi tạo dữ liệu!");
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(Role.ROLE_ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
        log.info(">>> [SEEDER] Tài khoản Admin: admin / 123456");
    }

    private void createDevData() {
        // --- 2.1 Tạo User tam_dev ---
        User user = new User();
        user.setUsername("tam_dev");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);
        // Gán Telegram Chat ID mẫu (Nếu Tâm đã biết Chat ID thật thì set vào đây luôn)
        user.setTelegramChatId("1040788604"); 
        user = userRepository.save(user);

        // --- 2.2 Tạo Thẻ tín dụng mặc định (FIX LỖI THIẾU THẺ) ---
        CreditCard card = new CreditCard();
        card.setCardName("Visa Cashback Tâm");
        card.setLimitAmount(new BigDecimal("50000000"));
        card.setStatementDay(25);
        card.setDueDateOffset(15);
        card.setUser(user); // Mối quan hệ ManyToOne
        creditCardRepository.save(card);
        log.info(">>> [SEEDER] Đã cấp thẻ mặc định cho tam_dev");

        // --- 2.3 Tạo Categories với Search Keywords (FIX LỖI 'LẠ QUÁ') ---
        Category c1 = saveCategory("Ăn uống", "🍲", "an uong, cafe, cà phê, bun bo, com tam, pho, tra sua", TransactionType.EXPENSE);
        Category c2 = saveCategory("Di chuyển", "🏍️", "di chuyen, grab, taxi, xang, xe om, be", TransactionType.EXPENSE);
        Category c3 = saveCategory("Tiền lương", "💵", "luong, thu nhap, bonus", TransactionType.INCOME);
        Category c4 = saveCategory("Mua sắm", "🛒", "mua sam, shopping, lazada, shopee, sieu thi", TransactionType.EXPENSE);

        // --- 2.4 Bơm Transactions mẫu ---
        List<Category> expenses = List.of(c1, c2, c4);
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            Transaction t = new Transaction();
            BigDecimal amount = BigDecimal.valueOf(20000 + random.nextInt(480000));
            LocalDateTime randomDate = LocalDateTime.now().minusDays(random.nextInt(60));
            
            t.setAmount(amount);
            t.setUser(user);
            t.setCard(card);
            t.setCategory(expenses.get(random.nextInt(expenses.size())));
            t.setDescription("Giao dịch mẫu #" + (i + 1));
            
            // Fix lỗi Hibernate: Set cả 2 cột ngày
            t.setCreatedAt(randomDate);
            t.setTransactionDate(randomDate); 
            
            transactionRepository.save(t);
        }
        log.info(">>> [SEEDER] Đã bơm 50 transactions mẫu.");
    }

    private Category saveCategory(String name, String icon, String keywords, TransactionType type) {
        Category c = new Category();
        c.setName(name);
        c.setIcon(icon);
        c.setType(type);
        // Lưu keywords đã normalize để Bot dễ tìm
        c.setSearchKeywords(VNCharacterUtils.normalizeForSearch(keywords)); 
        return categoryRepository.save(c);
    }
}