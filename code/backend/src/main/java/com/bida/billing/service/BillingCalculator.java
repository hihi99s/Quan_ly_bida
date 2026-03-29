package com.bida.billing.service;

import com.bida.billing.dto.BillingResult;
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
     * @throws IllegalStateException nếu không tìm thấy PriceRule cho cặp tableType + dayType
     */
    public BillingResult calculate(Session session) {
        if (session.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Session #" + session.getId() + " chua co endTime.");
        }

        DayType dayType = resolveDayType(session.getStartTime());
        String tableType = session.getTable().getTableType().toString();

        log.info("Tinh tien chinh thuc – session #{} | Ban: {} | Table Type: {} | Day Type: {}",
                session.getId(), session.getTable().getName(), tableType, dayType);

        try {
            BillingResult result = pricingStrategy.calculate(
                    session.getStartTime(),
                    session.getEndTime(),
                    session.getTable().getTableType(),
                    dayType
            );
            log.info("✓ Tính tiền thành công - Session #{} | Tổng: {} VND | {} segment(s)",
                    session.getId(), result.getTotalAmount(),
                    result.getSegments() != null ? result.getSegments().size() : 0);
            return result;
        } catch (RuntimeException e) {
            log.error("✗ LỖI TÍNH TIỀN - Session #{} (Ban: {}, Table Type: {}, Day Type: {}) - Chi tiết: {}",
                    session.getId(), session.getTable().getName(), tableType, dayType, e.getMessage(), e);
            throw e; // Rethrow để SessionService xử lý
        }
    }

    /**
     * Tinh tien tam thoi (preview) cho phien dang chay.
     * Phase 2: tru di totalPausedMinutes neu dang pause.
     */
    public BigDecimal calculateCurrentAmount(Session session) {
        LocalDateTime now = LocalDateTime.now();
        DayType dayType = resolveDayType(session.getStartTime());
        log.debug("Tinh tien tam – session #{} den {}", session.getId(), now);

        BillingResult result = pricingStrategy.calculate(
                session.getStartTime(),
                now,
                session.getTable().getTableType(),
                dayType
        );
        return result.getTotalAmount();
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
