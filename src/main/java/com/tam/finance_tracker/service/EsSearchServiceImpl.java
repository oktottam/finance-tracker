package com.tam.finance_tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EsSearchServiceImpl implements EsSearchService {
  private final ElasticsearchOperations elasticsearchOperations;

    @Override
    @CircuitBreaker(name = "esService", fallbackMethod = "fallbackESData")
    public List<String> getUnusualActivities(String username, LocalDate date) {
        // Giả sử mình tìm các giao dịch có note là "vượt hạn mức" hoặc "cảnh báo"
        // Hoặc các giao dịch có số tiền lớn bất thường trong tháng
        
        Criteria criteria = new Criteria("ownerUsername").is(username)
                .and("description").contains("cảnh báo")
                .and("transactionDate").greaterThanEqual(date.withDayOfMonth(1))
                .and("transactionDate").lessThanEqual(date.withDayOfMonth(date.lengthOfMonth()));

        CriteriaQuery query = new CriteriaQuery(criteria);

        var searchHits = elasticsearchOperations.search(query, Object.class, 
                org.springframework.data.elasticsearch.core.mapping.IndexCoordinates.of("transactions"));

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(obj -> "Phát hiện giao dịch lạ: " + obj.toString()) // Tâm có thể map sang DTO chuẩn hơn
                .collect(Collectors.toList());
    }

    // Hàm dự phòng (Fallback) - PHẢI CÓ Throwable t ở cuối
    public List<String> fallbackESData(String username, LocalDate date, Throwable t) {
        log.error(">>> Elasticsearch đang 'hắt hơi sổ mũi' cho user {}: {}", username, t.getMessage());
        
        // Thay vì để cả App văng lỗi 500, mình trả về một list thông báo nhẹ nhàng.
        // Điều này giúp ReportService vẫn chạy tiếp và lấy được số liệu từ Postgres.
        return List.of("Hệ thống phân tích đang bảo trì, Tâm vui lòng kiểm tra lại sau nhé!");
    }
}
