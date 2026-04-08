package com.tam.finance_tracker.service;

import java.time.LocalDate;
import java.util.List;

public interface EsSearchService {
    // Tìm các hoạt động "bất thường" (ví dụ: tiêu xài vượt mức trung bình hoặc các tag cảnh báo)
    List<String> getUnusualActivities(String username, LocalDate date);
}
