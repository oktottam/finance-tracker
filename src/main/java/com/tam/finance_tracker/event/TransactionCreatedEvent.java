package com.tam.finance_tracker.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event đại diện cho việc một giao dịch đã được tạo thành công trong DB.
 * "Simple is the best": Chỉ cần truyền ID là đủ để các bên khác tìm kiếm.
 */
@Getter
@RequiredArgsConstructor
public class TransactionCreatedEvent {
    private final String transactionId;
}