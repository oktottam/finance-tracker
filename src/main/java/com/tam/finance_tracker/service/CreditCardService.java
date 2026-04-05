package com.tam.finance_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.dto.CreditCardRequest;
import com.tam.finance_tracker.repository.CreditCardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditCardService {
private final CreditCardRepository creditCardRepository;

    // 1. Chỉ lấy thẻ của người dùng hiện tại
    @Transactional(readOnly = true)
    public List<CreditCard> getCardsByUsername(String username) {
        // Tâm cần thêm hàm này vào CreditCardRepository nhé
        return creditCardRepository.findByOwnerUsername(username); 
    }

    // 2. Tự động gán chủ sở hữu khi tạo thẻ
    @Transactional
    public CreditCard createCard(CreditCardRequest request, String username) {
        CreditCard card = new CreditCard();
        card.setCardName(request.getCardName());
        card.setLimitAmount(request.getLimitAmount());
        card.setOwnerUsername(username); // <--- Ép buộc chủ sở hữu là người đang login
        
        return creditCardRepository.save(card);
    }
}
