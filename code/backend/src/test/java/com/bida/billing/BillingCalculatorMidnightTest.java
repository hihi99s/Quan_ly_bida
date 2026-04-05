package com.bida.billing;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.service.BillingCalculator;
import com.bida.billing.strategy.PricingStrategy;
import com.bida.entity.AppSetting;
import com.bida.entity.BilliardTable;
import com.bida.entity.Session;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import com.bida.repository.AppSettingRepository;
import com.bida.repository.HolidayCalendarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingCalculator – Midnight Split Scenarios")
class BillingCalculatorMidnightTest {

    @Mock private PricingStrategy pricingStrategy;
    @Mock private AppSettingRepository appSettingRepository;
    @Mock private HolidayCalendarRepository holidayCalendarRepository;

    @InjectMocks
    private BillingCalculator billingCalculator;

    private BilliardTable mockTable;

    @BeforeEach
    void setUp() {
        mockTable = new BilliardTable();
        mockTable.setTableType(TableType.POOL);
        mockTable.setName("Bàn Test 01");

        // Default: no holiday mode
        lenient().when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.empty());
    }

    private Session createSession(LocalDateTime start, LocalDateTime end) {
        Session session = new Session();
        session.setId(100L);
        session.setTable(mockTable);
        session.setStartTime(start);
        session.setEndTime(end);
        return session;
    }

    private BillingResult result(double amount) {
        return BillingResult.builder()
                .totalAmount(BigDecimal.valueOf(amount))
                .segments(Collections.emptyList())
                .totalMinutes(0)
                .build();
    }

    @Test
    @DisplayName("Test 1: Phiên bình thường (20:00 - 22:30)")
    void test1_NormalSession() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 20, 0); // Thứ 4
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 22, 30);
        Session session = createSession(start, end);

        when(pricingStrategy.calculate(start, end, TableType.POOL, DayType.WEEKDAY))
                .thenReturn(result(150000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("150000");
        verify(pricingStrategy, times(1)).calculate(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Test 2: Phiên qua 00:00, WEEKDAY -> WEEKDAY")
    void test2_CrossMidnightWeekdayToWeekday() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 23, 23, 0); // Thứ 2
        LocalDateTime midnight = LocalDateTime.of(2026, 3, 24, 0, 0); // Thứ 3
        LocalDateTime end   = LocalDateTime.of(2026, 3, 24, 1, 0);
        Session session = createSession(start, end);

        // Sub-range 1: Mon 23:00 - Tue 00:00 (WEEKDAY)
        when(pricingStrategy.calculate(eq(start), eq(midnight), eq(TableType.POOL), eq(DayType.WEEKDAY)))
                .thenReturn(result(70000));
        // Sub-range 2: Tue 00:00 - Tue 01:00 (WEEKDAY)
        when(pricingStrategy.calculate(eq(midnight), eq(end), eq(TableType.POOL), eq(DayType.WEEKDAY)))
                .thenReturn(result(70000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("140000");
        verify(pricingStrategy, times(2)).calculate(any(), any(), any(), eq(DayType.WEEKDAY));
    }

    @Test
    @DisplayName("Test 3: Phiên qua 00:00, WEEKDAY -> WEEKEND")
    void test3_CrossMidnightWeekdayToWeekend() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 27, 23, 0); // Thứ 6 (Fri)
        LocalDateTime midnight = LocalDateTime.of(2026, 3, 28, 0, 0); // Thứ 7 (Sat)
        LocalDateTime end   = LocalDateTime.of(2026, 3, 28, 1, 30);
        Session session = createSession(start, end);

        // Sub-range 1: Fri 23:00 - Sat 00:00 (WEEKDAY)
        when(pricingStrategy.calculate(eq(start), eq(midnight), eq(TableType.POOL), eq(DayType.WEEKDAY)))
                .thenReturn(result(60000));
        // Sub-range 2: Sat 00:00 - Sat 01:30 (WEEKEND)
        when(pricingStrategy.calculate(eq(midnight), eq(end), eq(TableType.POOL), eq(DayType.WEEKEND)))
                .thenReturn(result(120000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("180000");
        verify(pricingStrategy).calculate(any(), any(), any(), eq(DayType.WEEKDAY));
        verify(pricingStrategy).calculate(any(), any(), any(), eq(DayType.WEEKEND));
    }

    @Test
    @DisplayName("Test 4: Phiên qua 00:00 vào ngày lễ (HolidayCalendar)")
    void test4_CrossMidnightToHoliday() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 30, 23, 0); 
        LocalDateTime midnight = LocalDateTime.of(2026, 5, 1, 0, 0); 
        LocalDateTime end   = LocalDateTime.of(2026, 5, 1, 1, 0);
        Session session = createSession(start, end);

        // Giả lập 1/5 là ngày lễ
        when(holidayCalendarRepository.isHoliday(eq(LocalDate.of(2026, 5, 1)), anyInt(), anyInt()))
                .thenReturn(true);

        when(pricingStrategy.calculate(eq(start), eq(midnight), eq(TableType.POOL), eq(DayType.WEEKDAY)))
                .thenReturn(result(60000));
        when(pricingStrategy.calculate(eq(midnight), eq(end), eq(TableType.POOL), eq(DayType.HOLIDAY)))
                .thenReturn(result(80000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("140000");
        verify(pricingStrategy).calculate(any(), any(), any(), eq(DayType.HOLIDAY));
    }

    @Test
    @DisplayName("Test 5: Holiday Mode ON qua 00:00")
    void test5_HolidayModeCrossMidnight() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 23, 0); 
        LocalDateTime end   = LocalDateTime.of(2026, 3, 26, 1, 0);
        Session session = createSession(start, end);

        // Bật Holiday Mode
        AppSetting holidaySetting = new AppSetting();
        holidaySetting.setSettingKey("HOLIDAY_MODE");
        holidaySetting.setSettingValue("true");
        when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.of(holidaySetting));

        // Cả 2 sub-range đều là HOLIDAY
        when(pricingStrategy.calculate(any(), any(), eq(TableType.POOL), eq(DayType.HOLIDAY)))
                .thenReturn(result(80000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("160000");
        verify(pricingStrategy, times(2)).calculate(any(), any(), any(), eq(DayType.HOLIDAY));
    }

    @Test
    @DisplayName("Test 7: Phiên kéo dài hơn 24h (Thứ 6 -> Thứ 7 -> Chủ nhật)")
    void test7_MultiDaySession() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 27, 23, 0); // Thứ 6
        LocalDateTime mid1  = LocalDateTime.of(2026, 3, 28, 0, 0);  // Thứ 7 (Weekend)
        LocalDateTime mid2  = LocalDateTime.of(2026, 3, 29, 0, 0);  // Chủ nhật (Weekend)
        LocalDateTime end    = LocalDateTime.of(2026, 3, 29, 0, 30);
        Session session = createSession(start, end);

        // Segment 1: Fri 23-00 (Weekday)
        when(pricingStrategy.calculate(eq(start), eq(mid1), any(), eq(DayType.WEEKDAY))).thenReturn(result(60000));
        // Segment 2: Sat 00-00 (Weekend)
        when(pricingStrategy.calculate(eq(mid1), eq(mid2), any(), eq(DayType.WEEKEND))).thenReturn(result(1000000));
        // Segment 3: Sun 00-00:30 (Weekend)
        when(pricingStrategy.calculate(eq(mid2), eq(end), any(), eq(DayType.WEEKEND))).thenReturn(result(30000));

        BillingResult result = billingCalculator.calculate(session);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("1090000");
        verify(pricingStrategy, times(3)).calculate(any(), any(), any(), any());
    }
}
