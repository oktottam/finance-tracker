package com.tam.finance_tracker.dto;

import java.math.BigDecimal;

public interface CategorySumProjection {
    String getCategory();
    BigDecimal getAmount();
}
