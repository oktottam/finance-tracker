package com.tam.finance_tracker.service;

import java.io.Writer;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.tam.finance_tracker.domain.Transaction;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportExportService {
    // Kỹ thuật này giúp RAM Server luôn ổn định dù xuất 10 hay 1 triệu bản ghi
    public void exportTransactionsToCsv(Writer writer, List<Transaction> transactions) {
        try {
            StatefulBeanToCsv<Transaction> beanToCsv = new StatefulBeanToCsvBuilder<Transaction>(writer)
                    .withSeparator(',')
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            log.info("Bắt đầu ghi dữ liệu xuống Stream I/O...");
            beanToCsv.write(transactions);
            log.info("Xuất file thành công!");
            
        } catch (Exception e) {
            log.error("Lỗi khi ghi file CSV cho Tâm: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo file báo cáo, Tâm check log nhé!");
        }
    }
}
