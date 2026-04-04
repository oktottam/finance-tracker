package com.tam.finance_tracker.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.tam.finance_tracker.document.TransactionDocument;

public interface TransactionSearchRepository extends ElasticsearchRepository<TransactionDocument, String> {
    // Elasticsearch sẽ tự động hỗ trợ tìm kiếm mờ (Fuzzy search) cực đỉnh
}
