package com.tam.finance_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm User theo username để phục vụ việc Login/Auth
    Optional<User> findByUsername(String username);

    // Kiểm tra xem username đã tồn tại chưa khi Đăng ký
    Boolean existsByUsername(String username);
}
