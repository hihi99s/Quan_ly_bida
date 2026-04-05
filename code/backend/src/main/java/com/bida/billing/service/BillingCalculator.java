package com.bida.billing.service;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.dto.BillingSegment;
import com.bida.billing.strategy.PricingStrategy;
import com.bida.entity.Session;
import com.bida.entity.enums.DayType;
import com.bida.repository.AppSettingRepository;
import com.bida.repository.HolidayCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade Service duy nhat ma cac tang khac goi de tinh tien phien choi bida.
 *
 * Phase 2: Ho tro Holiday Calendar tu nhan biet + Pause minutes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingCalculator {

    private static final String HOLIDAY_MODE_KEY = "HOLIDAY_MODE";

    private final PricingStrategy pricingStrategy;
    private final AppSettingRepository appSettingRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;

    /**
     * Tinh tien chinh thuc cho phien da ket thuc.
     *
     * Phase 3: Ho tro phien qua 00:00 – chia sub-range theo calendar day,
     * moi sub-range ap dung DayType cua ngay do (WEEKDAY / WEEKEND / HOLIDAY).
     *
     * @throws IllegalStateException nếu không tìm thấy PriceRule cho cặp tableType + dayType
     */
    public BillingResult calculate(Session session) {
        if (session.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Session #" + session.getId() + " chua co endTime.");
        }

        LocalDateTime start = session.getStartTime();
        LocalDateTime end   = session.getEndTime();

        // ── Phase 3: Neu phien da chuyen ban, tach tinh tien 2 giai doan ──────
        if (session.getTransferredAt() != null && session.getOriginalTableType() != null) {
            LocalDateTime transferPoint = session.getTransferredAt();

            log.info("Tinh tien chinh thuc (CHUYEN BAN) – session #{} | {} → [chuyen luc {}] → {}",
                    session.getId(), start, transferPoint, end);

            // Giai doan 1: startTime → transferredAt  (ban goc)
            BillingResult phase1 = calculateRange(session.getId(), start, transferPoint,
                    session.getOriginalTableType(), "GIAI DOAN 1 (ban goc)");

            // Giai doan 2: transferredAt → endTime    (ban moi)
            BillingResult phase2 = calculateRange(session.getId(), transferPoint, end,
                    session.getTable().getTableType(), "GIAI DOAN 2 (ban moi)");

            List<BillingSegment> allSegments = new ArrayList<>();
            allSegments.addAll(phase1.getSegments());
            allSegments.addAll(phase2.getSegments());

            BigDecimal total = phase1.getTotalAmount().add(phase2.getTotalAmount());
            long totalMinutes = allSegments.stream()
                    .mapToLong(BillingSegment::getDurationMinutes).sum();

            log.info("✓ Tinh tien CHUYEN BAN – session #{} | GD1: {} VND + GD2: {} VND = {} VND",
                    session.getId(), phase1.getTotalAmount(), phase2.getTotalAmount(), total);

            return BillingResult.builder()
                    .segments(allSegments)
                    .totalAmount(total)
                    .totalMinutes(totalMinutes)
                    .build();
        }

        // ── Phien binh thuong (khong chuyen ban) – giu nguyen logic cu ────────
        String tableType = session.getTable().getTableType().toString();
        log.info("Tinh tien chinh thuc – session #{} | Ban: {} | Table Type: {} | {} → {}",
                session.getId(), session.getTable().getName(), tableType, start, end);

        return calculateRange(session.getId(), start, end,
                session.getTable().getTableType(), "BINH THUONG");
    }

    /**
     * Tinh tien cho mot khoang thoi gian [start, end) voi loai ban cu the.
     * Su dung lai splitByCalendarDay hien co.
     */
    private BillingResult calculateRange(Long sessionId,
                                          LocalDateTime start, LocalDateTime end,
                                          com.bida.entity.enums.TableType tableType,
                                          String label) {
        List<LocalDateTime[]> ranges = splitByCalendarDay(start, end);
        List<BillingSegment> segments = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (LocalDateTime[] range : ranges) {
            DayType dayType = resolveDayType(range[0]);
            log.info("  [{}] Sub-range [{} → {}] | DayType: {}", label, range[0], range[1], dayType);
            try {
                BillingResult partial = pricingStrategy.calculate(range[0], range[1], tableType, dayType);
                segments.addAll(partial.getSegments());
                total = total.add(partial.getTotalAmount());
            } catch (RuntimeException e) {
                log.error("✗ LỖI TÍNH TIỀN - Session #{} [{}] sub-range [{} → {}] ({}, {}) - {}",
                        sessionId, label, range[0], range[1], tableType, dayType, e.getMessage(), e);
                throw e;
            }
        }

        long totalMinutes = segments.stream().mapToLong(BillingSegment::getDurationMinutes).sum();
        return BillingResult.builder()
                .segments(segments)
                .totalAmount(total)
                .totalMinutes(totalMinutes)
                .build();
    }

    /**
     * Tinh tien tam thoi (preview) cho phien dang chay.
     *
     * Phase 3: Ho tro ca truong hop chuyen ban va qua 00:00.
     */
    public BigDecimal calculateCurrentAmount(Session session) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = session.getStartTime();
        log.debug("Tinh tien tam – session #{} den {}", session.getId(), now);

        // Phase 3: Neu da chuyen ban, tach 2 giai doan cho preview
        if (session.getTransferredAt() != null && session.getOriginalTableType() != null) {
            LocalDateTime transferPoint = session.getTransferredAt();

            // Giai doan 1: startTime → transferredAt (da xong, co dinh)
            BigDecimal phase1 = sumRange(start, transferPoint, session.getOriginalTableType());

            // Giai doan 2: transferredAt → now (dang chay tren ban moi)
            BigDecimal phase2 = sumRange(transferPoint, now, session.getTable().getTableType());

            return phase1.add(phase2);
        }

        // Binh thuong
        return sumRange(start, now, session.getTable().getTableType());
    }

    /**
     * Helper: cong tien tren mot khoang thoi gian, bo qua sub-range loi.
     */
    private BigDecimal sumRange(LocalDateTime start, LocalDateTime end,
                                 com.bida.entity.enums.TableType tableType) {
        List<LocalDateTime[]> ranges = splitByCalendarDay(start, end);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDateTime[] range : ranges) {
            DayType dayType = resolveDayType(range[0]);
            try {
                BillingResult partial = pricingStrategy.calculate(range[0], range[1], tableType, dayType);
                total = total.add(partial.getTotalAmount());
            } catch (Exception e) {
                log.warn("Loi tinh tien tam sub-range [{} → {}]: {}", range[0], range[1], e.getMessage());
            }
        }
        return total;
    }

    /**
     * Chia khoang thoi gian [start, end) thanh cac sub-range tai moc 00:00 moi ngay.
     *
     * Vi du: Wed 23:59 → Sat 01:00 tra ve:
     *   [Wed 23:59, Thu 00:00)
     *   [Thu 00:00, Fri 00:00)
     *   [Fri 00:00, Sat 00:00)
     *   [Sat 00:00, Sat 01:00)
     *
     * Truong hop pho bien (phien khong qua 00:00) tra ve dung 1 phan tu.
     */
    private List<LocalDateTime[]> splitByCalendarDay(LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime[]> ranges = new ArrayList<>();
        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalDateTime midnight = cursor.toLocalDate().plusDays(1).atStartOfDay();
            LocalDateTime rangeEnd = midnight.isBefore(end) ? midnight : end;
            ranges.add(new LocalDateTime[]{cursor, rangeEnd});
            cursor = rangeEnd;
        }
        return ranges;
    }

    /**
     * Xac dinh DayType tu mot thoi diem cu the.
     *
     * Priority:
     * 1. HOLIDAY_MODE (admin bat thu cong) → HOLIDAY
     * 2. HolidayCalendar tu nhan biet → HOLIDAY
     * 3. Thu 7 / Chu nhat → WEEKEND
     * 4. Mac dinh → WEEKDAY
     */
    public DayType resolveDayType(LocalDateTime dateTime) {
        // Uu tien 1: Holiday mode (admin bat thu cong)
        boolean isHolidayMode = appSettingRepository.findBySettingKey(HOLIDAY_MODE_KEY)
                .map(setting -> "true".equalsIgnoreCase(setting.getSettingValue()))
                .orElse(false);

        if (isHolidayMode) {
            log.debug("DayType = HOLIDAY (HOLIDAY_MODE dang bat)");
            return DayType.HOLIDAY;
        }

        // Uu tien 2: Holiday Calendar tu nhan biet
        try {
            boolean isCalendarHoliday = holidayCalendarRepository.isHoliday(
                    dateTime.toLocalDate(),
                    dateTime.getMonthValue(),
                    dateTime.getDayOfMonth()
            );
            if (isCalendarHoliday) {
                log.debug("DayType = HOLIDAY (HolidayCalendar)");
                return DayType.HOLIDAY;
            }
        } catch (Exception e) {
            log.warn("Loi kiem tra HolidayCalendar: {}", e.getMessage());
        }

        // Uu tien 3: Cuoi tuan
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.debug("DayType = WEEKEND ({})", dayOfWeek);
            return DayType.WEEKEND;
        }

        // Mac dinh: Ngay thuong
        log.debug("DayType = WEEKDAY ({})", dayOfWeek);
        return DayType.WEEKDAY;
    }

    @Deprecated(since = "1.1", forRemoval = true)
    public DayType getCurrentDayType(LocalDateTime dateTime) {
        return resolveDayType(dateTime);
    }
}
