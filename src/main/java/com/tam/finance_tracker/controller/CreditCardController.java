package com.tam.finance_tracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Phải login mới được tạo
    public ResponseEntity<CreditCard> createCard(@Valid @RequestBody CreditCardRequest request) {
        // Lấy tên người đang login để gán làm chủ thẻ
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Đẩy toàn bộ logic tạo và gán User xuống Service cho sạch
        return ResponseEntity.ok(creditCardService.createCard(request, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CreditCard>> getAllCards() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Chỉ lấy thẻ của ĐÚNG người đang login
        return ResponseEntity.ok(creditCardService.getCardsByUsername(currentUser));
    }
}
