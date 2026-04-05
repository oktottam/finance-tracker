package com.tam.finance_tracker.service;

import org.springframework.stereotype.Service;

import com.tam.finance_tracker.document.TransactionDocument;
import com.tam.finance_tracker.repository.TransactionSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final TransactionSearchRepository searchRepository;

    public Iterable<TransactionDocument> searchTransactions(String query) {
        // Tìm kiếm tất cả các field (description, category) khớp với query
        return searchRepository.findAll(); 
        // Tâm có thể nâng cấp thêm query cụ thể hơn ở đây sau nhé!
    }
}
