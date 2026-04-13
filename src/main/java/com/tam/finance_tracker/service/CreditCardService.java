package com.tam.finance_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.domain.User;
import com.tam.finance_tracker.dto.CreditCardRequest;
import com.tam.finance_tracker.repository.CreditCardRepository;
import com.tam.finance_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository; // Inject thêm để tìm User

    @Transactional
    public CreditCard createCard(CreditCardRequest request, String username) {
        // Tìm User entity từ username người đang login
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));

        CreditCard card = new CreditCard();
        card.setCardName(request.getCardName());
        card.setLimitAmount(request.getLimitAmount());
        card.setStatementDay(request.getStatementDay());
        card.setDueDateOffset(request.getDueDateOffset());
        
        // SET OBJECT USER VÀO ĐÂY
        card.setUser(user); 
        
        return creditCardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public List<CreditCard> getCardsByUsername(String username) {
        return creditCardRepository.findByUser_Username(username);
    }
}
