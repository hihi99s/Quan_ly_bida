package com.bida.billing.strategy;

import com.bida.billing.dto.BillingResult;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;

import java.time.LocalDateTime;

/**
 * Strategy interface cho việc tính tiền phiên chơi bida.
 *
 * <p>Áp dụng Strategy Pattern để dễ dàng thay thế hoặc mở rộng
 * thuật toán tính giá (ví dụ: theo khung giờ, flat rate, happy hour…)
 * mà không thay đổi code phía caller.</p>
 *
 * <p>Implementations phải là Spring Bean (@Service / @Component).</p>
 */
public interface PricingStrategy {

    /**
     * Tính tiền cho một khoảng thời gian chơi.
     *
     * @param start     Thời điểm bắt đầu phiên (inclusive)
     * @param end       Thời điểm kết thúc phiên (exclusive)
     * @param tableType Loại bàn (POOL, CAROM, VIP)
     * @param dayType   Loại ngày (WEEKDAY, WEEKEND, HOLIDAY)
     * @return {@link BillingResult} chứa danh sách segment và tổng tiền
     * @throws RuntimeException nếu không tìm thấy PriceRule phù hợp
     */
    BillingResult calculate(LocalDateTime start,
                            LocalDateTime end,
                            TableType tableType,
                            DayType dayType);
}
