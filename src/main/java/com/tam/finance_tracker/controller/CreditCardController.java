package com.tam.finance_tracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.dto.CreditCardRequest;
import com.tam.finance_tracker.service.CreditCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/cards") 
@RequiredArgsConstructor
public class CreditCardController {
    private final CreditCardService creditCardService;

    @PostMapping
    public ResponseEntity<CreditCard> createCard(@Valid @RequestBody CreditCardRequest request) {
        CreditCard card = new CreditCard();
        card.setCardName(request.getCardName());
        card.setLimitAmount(request.getLimitAmount());
        card.setStatementDay(request.getStatementDay());
        card.setDueDateOffset(request.getDueDateOffset());
        
        return ResponseEntity.ok(creditCardService.createCard(card));
    }

    @GetMapping
    public ResponseEntity<List<CreditCard>> getAllCards() {
        return ResponseEntity.ok(creditCardService.getAllCards());
    }
}
