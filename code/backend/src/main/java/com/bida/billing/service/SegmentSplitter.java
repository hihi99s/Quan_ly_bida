package com.bida.billing.service;

import com.bida.billing.dto.BillingSegment;
import com.bida.entity.PriceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service chia khoảng thời gian chơi thành các {@link BillingSegment}
 * theo danh sách PriceRule đã được sắp xếp theo startTime tăng dần.
 *
 * <h3>Thuật toán cursor:</h3>
 * <ol>
 *   <li>Bắt đầu từ {@code cursor = start}</li>
 *   <li>Tìm PriceRule bao trùm {@code cursor.toLocalTime()}</li>
 *   <li>Tính segmentEnd = MIN(end, boundary của rule hiện tại)</li>
 *   <li>Tính durationMinutes và amount (CEILING)</li>
 *   <li>Tạo BillingSegment, dịch cursor, lặp lại</li>
 * </ol>
 *
 * <h3>Edge cases được xử lý:</h3>
 * <ul>
 *   <li>Phiên rất ngắn (< 5 phút)</li>
 *   <li>Rule bao qua nửa đêm (endTime ≤ startTime)</li>
 *   <li>Phiên cross ngày (nhiều ngày liên tiếp)</li>
 * </ul>
 */
@Slf4j
@Service
public class SegmentSplitter {

    /**
     * Chia khoảng [{@code start}, {@code end}) thành danh sách segment,
     * mỗi segment áp đúng một PriceRule.
     *
     * @param start Thời điểm bắt đầu phiên (inclusive)
     * @param end   Thời điểm kết thúc phiên (exclusive)
     * @param rules Danh sách PriceRule đã sort theo startTime ASC
     * @return Danh sách BillingSegment không rỗng
     * @throws RuntimeException nếu không có rule phù hợp tại một thời điểm nào đó
     */
    public List<BillingSegment> split(LocalDateTime start,
                                      LocalDateTime end,
                                      List<PriceRule> rules) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("start và end không được null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "endTime phải sau startTime: start=" + start + ", end=" + end);
        }

        List<BillingSegment> segments = new ArrayList<>();
        LocalDateTime cursor = start;

        while (cursor.isBefore(end)) {

            LocalTime cursorTime = cursor.toLocalTime();
            PriceRule matchedRule = findRuleForTime(cursorTime, rules);

            if (matchedRule == null) {
                throw new RuntimeException(
                        "Không tìm thấy bảng giá cho thời điểm: " + cursorTime
                        + " (phiên bắt đầu " + start + ")");
            }

            // --- Xác định segmentEnd (boundary của rule hiện tại) ---
            LocalDateTime ruleEnd = computeRuleEnd(cursor, matchedRule);

            // Lấy min(end, ruleEnd) – không vượt quá endTime của phiên
            LocalDateTime segmentEnd = end.isBefore(ruleEnd) ? end : ruleEnd;

            // Phòng vòng lặp vô hạn: nếu segmentEnd == cursor thì nhảy 1 phút
            if (!segmentEnd.isAfter(cursor)) {
                log.warn("segmentEnd không vượt cursor tại {}; nhảy +1 phút để tránh loop", cursor);
                cursor = cursor.plusMinutes(1);
                continue;
            }

            // --- Tính thời lượng và thành tiền ---
            long durationMinutes = ChronoUnit.MINUTES.between(cursor, segmentEnd);

            // amount = ceil( durationMinutes * pricePerHour / 60 )
            BigDecimal amount = BigDecimal.valueOf(durationMinutes)
                    .multiply(matchedRule.getPricePerHour())
                    .divide(BigDecimal.valueOf(60), 0, RoundingMode.CEILING);

            BillingSegment segment = BillingSegment.builder()
                    .startTime(cursor)
                    .endTime(segmentEnd)
                    .durationMinutes(durationMinutes)
                    .pricePerHour(matchedRule.getPricePerHour())
                    .amount(amount)
                    .build();

            segments.add(segment);
            log.debug("Segment: {} → {} | {}p | {}/h | = {}",
                    cursor, segmentEnd, durationMinutes,
                    matchedRule.getPricePerHour(), amount);

            cursor = segmentEnd;
        }

        return segments;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Tìm PriceRule áp dụng cho {@code time}.
     *
     * <p>Hai trường hợp:</p>
     * <ul>
     *   <li>Rule bình thường: startTime &lt; endTime – time nằm trong [start, end)</li>
     *   <li>Rule qua nửa đêm: endTime &le; startTime – time &ge; start HOẶC time &lt; end</li>
     * </ul>
     */
    private PriceRule findRuleForTime(LocalTime time, List<PriceRule> rules) {
        for (PriceRule rule : rules) {
            LocalTime s = rule.getStartTime();
            LocalTime e = rule.getEndTime();

            if (isOvernight(rule)) {
                // Qua nửa đêm: [start, 24:00) ∪ [00:00, end)
                if (!time.isBefore(s) || time.isBefore(e)) {
                    return rule;
                }
            } else {
                // Bình thường: [start, end)
                if (!time.isBefore(s) && time.isBefore(e)) {
                    return rule;
                }
            }
        }
        return null;
    }

    /**
     * Tính thời điểm kết thúc của rule hiện tại dựa theo ngày của cursor.
     *
     * <ul>
     *   <li>Rule bình thường: ruleEnd = cursor.date + rule.endTime (cùng ngày)</li>
     *   <li>Rule qua nửa đêm AND cursor đang ở phần "sau nửa đêm" (time &lt; rule.endTime):
     *       ruleEnd = cursor.date + rule.endTime (cùng ngày)</li>
     *   <li>Rule qua nửa đêm AND cursor đang ở phần "trước nửa đêm":
     *       ruleEnd = cursor.date + 1 ngày (tức là 00:00 ngày hôm sau + rule.endTime)</li>
     * </ul>
     */
    private LocalDateTime computeRuleEnd(LocalDateTime cursor, PriceRule rule) {
        LocalDate cursorDate = cursor.toLocalDate();
        LocalTime cursorTime = cursor.toLocalTime();

        if (isOvernight(rule)) {
            // Nếu cursor đang ở phần sau nửa đêm [00:00, endTime) → kết thúc ngay hôm nay
            if (cursorTime.isBefore(rule.getEndTime())) {
                return cursorDate.atTime(rule.getEndTime());
            }
            // Cursor đang ở phần trước nửa đêm [startTime, 24:00) → kết thúc sang ngày hôm sau
            return cursorDate.plusDays(1).atTime(rule.getEndTime());
        }

        // Rule bình thường: endTime cùng ngày với cursor
        return cursorDate.atTime(rule.getEndTime());
    }

    /**
     * Kiểm tra rule có phải "qua nửa đêm" (overnight) không.
     * Rule overnight khi endTime &le; startTime
     * (ví dụ: 22:00 → 02:00 là overnight).
     */
    private boolean isOvernight(PriceRule rule) {
        // endTime == startTime cũng coi là overnight (24h rule – hiếm nhưng phòng ngừa)
        return !rule.getEndTime().isAfter(rule.getStartTime());
    }
}
