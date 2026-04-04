package com.tam.finance_tracker.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(indexName = "transactions") // Tên "bảng" trong Elasticsearch
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class TransactionDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description; // Để search "Mua cafe", "Đóng tiền điện"...

    private BigDecimal amount;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
}
