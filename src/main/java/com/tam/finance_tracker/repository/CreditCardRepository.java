package com.tam.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.CreditCard;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // Chỉ cần thế này thôi, Tâm đã có sẵn các hàm save(), findAll(), deleteById()...
}
