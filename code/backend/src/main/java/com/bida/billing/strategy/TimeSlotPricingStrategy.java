package com.bida.billing.strategy;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.dto.BillingSegment;
import com.bida.billing.service.SegmentSplitter;
import com.bida.entity.PriceRule;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import com.bida.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation của {@link PricingStrategy} theo cơ chế khung giờ (time-slot).
 *
 * <p>Luồng xử lý:</p>
 * <ol>
 *   <li>Lấy danh sách {@link PriceRule} theo {@code tableType} + {@code dayType} từ DB</li>
 *   <li>Gọi {@link SegmentSplitter#split} để chia phiên thành các segment</li>
 *   <li>Tổng hợp tổng tiền và tổng phút</li>
 * </ol>
 *
 * <p>Đây là bean {@code @Primary} khi chỉ có một implementation –
 * nếu sau này thêm strategy mới (FlatRate, HappyHour…) thì đánh dấu
 * {@code @Primary} cho implementation mặc định muốn dùng.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotPricingStrategy implements PricingStrategy {

    private final PriceRuleRepository priceRuleRepository;
    private final SegmentSplitter segmentSplitter;

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException nếu không có PriceRule nào cho cặp tableType/dayType,
     *                          hoặc nếu SegmentSplitter không tìm được rule tại một thời điểm cụ thể.
     */
    @Override
    public BillingResult calculate(LocalDateTime start,
                                   LocalDateTime end,
                                   TableType tableType,
                                   DayType dayType) {

        log.info("TimeSlotPricingStrategy.calculate – [{} / {}] từ {} → {}",
                tableType, dayType, start, end);

        // 1. Load price rules từ DB (đã sort theo startTime ASC)
        List<PriceRule> rules = priceRuleRepository
                .findByTableTypeAndDayTypeOrderByStartTimeAsc(tableType, dayType);

        if (rules == null || rules.isEmpty()) {
            String errorMsg = String.format(
                    "Không tìm thấy bảng giá cho loại bàn [%s] và loại ngày [%s]. " +
                    "Vui lòng kiểm tra bảng giá trong admin panel.",
                    tableType, dayType);
            log.error("✗ {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        log.debug("Query trả về {} rules cho [{} / {}]", rules.size(), tableType, dayType);

        // 2. Chia thành các segment theo khung giờ
        List<BillingSegment> segments = segmentSplitter.split(start, end, rules);

        // 3. Tổng hợp kết quả
        BigDecimal totalAmount = segments.stream()
                .map(BillingSegment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalMinutes = segments.stream()
                .mapToLong(BillingSegment::getDurationMinutes)
                .sum();

        log.info("✓ Kết quả: {} segment(s) | {}p | tổng = {} VND",
                segments.size(), totalMinutes, totalAmount);

        return BillingResult.builder()
                .segments(segments)
                .totalAmount(totalAmount)
                .totalMinutes(totalMinutes)
                .build();
    }
}
