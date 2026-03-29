package com.bida.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng hợp kết quả tính tiền cho một phiên chơi bida.
 * Bao gồm danh sách các segment chi tiết và tổng cộng.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingResult {

    /** Danh sách segment chi tiết (mỗi segment ứng với một PriceRule). */
    private List<BillingSegment> segments;

    /** Tổng tiền phiên = tổng amount của tất cả segment. */
    private BigDecimal totalAmount;

    /** Tổng thời gian chơi (phút). */
    private long totalMinutes;
}
