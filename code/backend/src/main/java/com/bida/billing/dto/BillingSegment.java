package com.bida.billing.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO đại diện cho một đoạn thời gian (segment) trong phiên chơi bida,
 * ánh xạ với đúng một PriceRule tại thời điểm đó.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingSegment {

    /** Thời điểm bắt đầu segment. */
    private LocalDateTime startTime;

    /** Thời điểm kết thúc segment. */
    private LocalDateTime endTime;

    /** Thời lượng thực tế của segment (phút). */
    private long durationMinutes;

    /** Đơn giá áp dụng cho segment này (VND/giờ). */
    private BigDecimal pricePerHour;

    /**
     * Thành tiền = (durationMinutes / 60) * pricePerHour,
     * làm tròn LÊN (CEILING) đến đồng nguyên.
     */
    private BigDecimal amount;
}
