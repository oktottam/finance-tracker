package com.tam.finance_tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.dto.TransactionRequest;
import com.tam.finance_tracker.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        // Controller bây giờ chỉ đóng vai trò điều hướng, cực sạch!
        return ResponseEntity.ok(transactionService.createTransactionFromRequest(request));
    }
}