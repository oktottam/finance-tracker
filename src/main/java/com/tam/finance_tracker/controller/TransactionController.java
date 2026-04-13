package com.tam.finance_tracker.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.tam.finance_tracker.domain.Transaction;
import com.tam.finance_tracker.dto.TransactionRequest;
import com.tam.finance_tracker.dto.MonthlySummary;
import com.tam.finance_tracker.service.TransactionService;
import com.tam.finance_tracker.repository.TransactionRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository; // Inject thêm để dùng các query thống kê

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransactionFromRequest(request));
    }

    // 1. Lấy danh sách giao dịch (Hiện GET trên Swagger)
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }

    // 2. Lấy thống kê hàng tháng (Hiện GET /summary trên Swagger)
    @GetMapping("/summary")
    public ResponseEntity<MonthlySummary> getMonthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int month,
            @RequestParam int year) {
        
        // Gọi hàm default mượt mà bạn đã viết trong Repository
        return ResponseEntity.ok(transactionRepository.calculateSummary(
                userDetails.getUsername(), month, year));
    }
}