package com.tam.finance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.CreditCard;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // Chỉ cần thế này thôi, Tâm đã có sẵn các hàm save(), findAll(), deleteById()...

    // Spring sẽ tự hiểu: "SELECT * FROM credit_cards WHERE owner_username = ?"
    List<CreditCard> findByOwnerUsername(String username);

    // Kiểm tra xem thẻ có thuộc quyền sở hữu của User không trước khi Xóa/Sửa
    boolean existsByIdAndOwnerUsername(Long id, String username);
}
