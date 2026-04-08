package com.tam.finance_tracker.controller;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.dto.ReportDTO;
import com.tam.finance_tracker.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('USER')")
    public CompletableFuture<ResponseEntity<ReportDTO>> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return reportService.generateMonthlyReport(username, date)
                .thenApply(ResponseEntity::ok);
    }
}
