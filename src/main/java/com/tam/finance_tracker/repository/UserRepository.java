package com.tam.finance_tracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.Role;
import com.tam.finance_tracker.domain.User;

import jakarta.persistence.QueryHint;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm User theo username để phục vụ việc Login/Auth
    Optional<User> findByUsername(String username);

    // Kiểm tra xem username đã tồn tại chưa khi Đăng ký
    Boolean existsByUsername(String username);

    // QUAN TRỌNG: Dùng để nhận diện User khi họ chat hoặc nhấn nút trên Telegram
    Optional<User> findByTelegramChatId(String telegramChatId);

    // Thêm phương thức này để bắt mã từ lệnh /start của Telegram
    Optional<User> findByVerificationToken(String verificationToken);

    // Tìm tất cả User đã thực hiện link tài khoản Telegram thành công
    List<User> findAllByTelegramChatIdIsNotNull();

    // Nếu muốn an toàn hơn, Tâm có thể tìm theo Role nữa
    List<User> findAllByTelegramChatIdIsNotNullAndRole(Role role);

    @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100"))
    @Query("SELECT u FROM User u WHERE u.telegramChatId IS NOT NULL")
    Stream<User> streamAllByTelegramChatIdIsNotNull();
}
