package com.tam.finance_tracker.controller;

import java.io.IOException;
import java.io.Writer;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.repository.TransactionRepository;
import com.tam.finance_tracker.service.ReportExportService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportExportController {
    private final ReportExportService exportService;
    private final TransactionRepository transactionRepo;

    @GetMapping("/download/transactions")
    public void downloadTransactions(HttpServletResponse response) throws IOException {
        // 1. Thiết lập thông tin file
        String fileName = "transactions_report.csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // 2. Lấy writer từ response - Đây chính là "đường ống" nối tới máy người dùng
        Writer writer = response.getWriter();

        // 3. Lấy dữ liệu (Trong thực tế nên dùng Stream từ JPA để tối ưu hơn nữa)
        var transactions = transactionRepo.findAll();

        // 4. Đẩy vào Service để "bơm" dữ liệu ra ống dẫn
        exportService.exportTransactionsToCsv(writer, transactions);
    }
}
