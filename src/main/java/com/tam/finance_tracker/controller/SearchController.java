package com.tam.finance_tracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.document.TransactionDocument;
import com.tam.finance_tracker.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/transactions")
    public Iterable<TransactionDocument> search(@RequestParam String q) {
        return searchService.searchTransactions(q);
    }
}
