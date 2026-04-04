package com.tam.finance_tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.repository.CreditCardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;

    @Transactional(readOnly = true)
    public List<CreditCard> getAllCards() {
        return creditCardRepository.findAll();
    }

    @Transactional
    public CreditCard createCard(CreditCard card) {
        return creditCardRepository.save(card);
    }
}
