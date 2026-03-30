package com.bida.billing;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.dto.BillingSegment;
import com.bida.billing.service.BillingCalculator;
import com.bida.billing.service.SegmentSplitter;
import com.bida.entity.AppSetting;
import com.bida.entity.BilliardTable;
import com.bida.entity.PriceRule;
import com.bida.entity.Session;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import com.bida.repository.AppSettingRepository;
import com.bida.repository.HolidayCalendarRepository;
import com.bida.repository.PriceRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho {@link BillingCalculator} và {@link SegmentSplitter}.
 *
 * <p>Sử dụng Mockito để giả lập DB, không phụ thuộc Spring context.
 * Mỗi test độc lập và có thể chạy offline.</p>
 *
 * <h3>Dữ liệu giá mẫu (Pool / Weekday):</h3>
 * <pre>
 *   08:00 – 12:00  →  40,000 VND/h
 *   12:00 – 17:00  →  50,000 VND/h
 *   17:00 – 22:00  →  60,000 VND/h
 *   22:00 – 08:00  →  70,000 VND/h  (overnight)
 * </pre>
 *
 * <h3>Dữ liệu giá mẫu (Pool / Weekend):</h3>
 * <pre>
 *   08:00 – 22:00  →  50,000 VND/h  (flat)
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingCalculator – Unit Tests")
class BillingCalculatorTest {

    // =========================================================================
    // Mocks & subject under test
    // =========================================================================

    @Mock
    private PriceRuleRepository priceRuleRepository;

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private HolidayCalendarRepository holidayCalendarRepository;

    /**
     * SegmentSplitter là pure logic (không có dependency DB),
     * nên inject real instance để test end-to-end algorithm.
     */
    private SegmentSplitter segmentSplitter;

    private BillingCalculator billingCalculator;

    // =========================================================================
    // Price rules helpers
    // =========================================================================

    /** Tạo PriceRule (non-overnight). */
    private PriceRule rule(LocalTime start, LocalTime end, int pricePerHour) {
        PriceRule r = new PriceRule();
        r.setTableType(TableType.POOL);
        r.setDayType(DayType.WEEKDAY);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setPricePerHour(BigDecimal.valueOf(pricePerHour));
        return r;
    }

    /** Tạo Session mock với BilliardTable POOL. */
    private Session sessionOf(LocalDateTime start, LocalDateTime end) {
        BilliardTable table = new BilliardTable();
        table.setTableType(TableType.POOL);

        Session session = new Session();
        session.setId(1L);
        session.setTable(table);
        session.setStartTime(start);
        session.setEndTime(end);
        return session;
    }

    /**
     * Danh sách rules cho Pool / Weekday:
     * 08:00–12:00 = 40k, 12:00–17:00 = 50k, 17:00–22:00 = 60k, 22:00–08:00 = 70k (overnight).
     */
    private List<PriceRule> weekdayRules() {
        PriceRule r1 = rule(LocalTime.of(8, 0),  LocalTime.of(12, 0), 40_000);
        PriceRule r2 = rule(LocalTime.of(12, 0), LocalTime.of(17, 0), 50_000);
        PriceRule r3 = rule(LocalTime.of(17, 0), LocalTime.of(22, 0), 60_000);
        // Overnight: 22:00 → 08:00
        PriceRule r4 = rule(LocalTime.of(22, 0), LocalTime.of(8, 0),  70_000);
        return List.of(r1, r2, r3, r4);
    }

    /** Danh sách rules cho Pool / Weekend: 08:00–22:00 = 50k. */
    private List<PriceRule> weekendRules() {
        PriceRule r1 = rule(LocalTime.of(8, 0), LocalTime.of(22, 0), 50_000);
        // Overnight: 22:00 → 08:00 = 70k
        PriceRule r2 = rule(LocalTime.of(22, 0), LocalTime.of(8, 0), 70_000);
        r1.setDayType(DayType.WEEKEND);
        r2.setDayType(DayType.WEEKEND);
        return List.of(r1, r2);
    }

    @BeforeEach
    void setUp() {
        segmentSplitter = new SegmentSplitter();

        // Xây dựng BillingCalculator với TimeSlotPricingStrategy thật
        com.bida.billing.strategy.TimeSlotPricingStrategy strategy =
                new com.bida.billing.strategy.TimeSlotPricingStrategy(priceRuleRepository, segmentSplitter);

        billingCalculator = new BillingCalculator(strategy, appSettingRepository, holidayCalendarRepository);

        // Mặc định: không phải ngày lễ
        lenient().when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.empty());
    }

    // =========================================================================
    // Test 1: Phiên trong 1 khung giờ
    // =========================================================================

    @Test
    @DisplayName("Phiên trong 1 khung giờ – Pool ngày thường 10:00–11:30 = 60,000 VND")
    void testSingleSlot() {
        // --- Arrange ---
        // Thứ Tư (weekday) 10:00 → 11:30 = 90 phút × 40,000/h
        // Kỳ vọng: amount = ceil(90 * 40000 / 60) = ceil(60000) = 60,000
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 10, 0);  // Thứ Tư
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 11, 30);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result).isNotNull();
        assertThat(result.getSegments()).hasSize(1);
        assertThat(result.getTotalMinutes()).isEqualTo(90L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(60_000));

        BillingSegment seg = result.getSegments().get(0);
        assertThat(seg.getDurationMinutes()).isEqualTo(90L);
        assertThat(seg.getPricePerHour()).isEqualByComparingTo(BigDecimal.valueOf(40_000));
        assertThat(seg.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(60_000));
    }

    // =========================================================================
    // Test 2: Phiên cross khung giờ
    // =========================================================================

    @Test
    @DisplayName("Phiên cross khung giờ – Pool ngày thường 11:00–13:00 = 2 segment = 90,000 VND")
    void testCrossSlot() {
        // --- Arrange ---
        // 11:00–12:00 = 60p × 40k/h = 40,000
        // 12:00–13:00 = 60p × 50k/h = 50,000
        // Tổng = 90,000
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 11, 0);  // Thứ Tư
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 13, 0);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(2);
        assertThat(result.getTotalMinutes()).isEqualTo(120L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(90_000));

        BillingSegment seg1 = result.getSegments().get(0);
        assertThat(seg1.getStartTime()).isEqualTo(start);
        assertThat(seg1.getEndTime()).isEqualTo(LocalDateTime.of(2026, 3, 25, 12, 0));
        assertThat(seg1.getDurationMinutes()).isEqualTo(60L);
        assertThat(seg1.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40_000));

        BillingSegment seg2 = result.getSegments().get(1);
        assertThat(seg2.getStartTime()).isEqualTo(LocalDateTime.of(2026, 3, 25, 12, 0));
        assertThat(seg2.getEndTime()).isEqualTo(end);
        assertThat(seg2.getDurationMinutes()).isEqualTo(60L);
        assertThat(seg2.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    // =========================================================================
    // Test 3: Phiên rất ngắn
    // =========================================================================

    @Test
    @DisplayName("Phiên ngắn – Pool ngày thường 10:00–10:15 = 10,000 VND (làm tròn CEILING)")
    void testShortSession() {
        // --- Arrange ---
        // 15 phút × 40,000/h = ceil(15 * 40000 / 60) = ceil(10000) = 10,000
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 10, 0);  // Thứ Tư
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 10, 15);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(1);
        assertThat(result.getTotalMinutes()).isEqualTo(15L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
    }

    @Test
    @DisplayName("Phiên cực ngắn – Pool ngày thường 10:00–10:05 = làm tròn CEILING lên 3,334 VND")
    void testVeryShortSession() {
        // --- Arrange ---
        // 5 phút × 40,000/h = ceil(5 * 40000 / 60) = ceil(3333.33) = 3,334
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 10, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 10, 5);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(1);
        assertThat(result.getTotalMinutes()).isEqualTo(5L);
        // ceil(5 * 40000 / 60) = ceil(3333.33) = 3334
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(3_334));
    }

    // =========================================================================
    // Test 4: Weekend pricing
    // =========================================================================

    @Test
    @DisplayName("Cuối tuần – Pool Thứ Bảy 10:00–11:00 = 50,000 VND")
    void testWeekendPricing() {
        // --- Arrange ---
        // Thứ Bảy (WEEKEND), 10:00–11:00 = 60p × 50k/h = 50,000
        LocalDateTime start = LocalDateTime.of(2026, 3, 28, 10, 0);  // Thứ Bảy
        LocalDateTime end   = LocalDateTime.of(2026, 3, 28, 11, 0);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKEND))
                .thenReturn(weekendRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(1);
        assertThat(result.getTotalMinutes()).isEqualTo(60L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
    }

    // =========================================================================
    // Test 5 (Bonus): Holiday mode override
    // =========================================================================

    @Test
    @DisplayName("Ngày lễ – HOLIDAY_MODE bật ghi đè, Pool Thứ Tư 10:00–11:00 áp giá Holiday")
    void testHolidayModeOverride() {
        // --- Arrange ---
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 10, 0);  // Thứ Tư thường
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 11, 0);
        Session session = sessionOf(start, end);

        // Bật HOLIDAY_MODE
        AppSetting holidaySetting = new AppSetting();
        holidaySetting.setSettingKey("HOLIDAY_MODE");
        holidaySetting.setSettingValue("true");
        when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.of(holidaySetting));

        // Rule holiday: 60k/h
        PriceRule holidayRule = rule(LocalTime.of(8, 0), LocalTime.of(22, 0), 60_000);
        holidayRule.setDayType(DayType.HOLIDAY);
        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.HOLIDAY))
                .thenReturn(List.of(holidayRule));

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(60_000));

        // Verify DayType đã là HOLIDAY chứ không phải WEEKDAY
        verify(priceRuleRepository, never())
                .findByTableTypeAndDayTypeOrderByStartTimeAsc(TableType.POOL, DayType.WEEKDAY);
        verify(priceRuleRepository, times(1))
                .findByTableTypeAndDayTypeOrderByStartTimeAsc(TableType.POOL, DayType.HOLIDAY);
    }

    // =========================================================================
    // Test 6 (Bonus): Phiên cross khung giờ 3 segment
    // =========================================================================

    @Test
    @DisplayName("Cross 3 khung giờ – Pool ngày thường 11:30–18:30 = 3 segment = 300,000 VND")
    void testTripleSlotCross() {
        // --- Arrange ---
        // 11:30–12:00 = 30p × 40k = 20,000
        // 12:00–17:00 = 300p × 50k = 250,000
        // 17:00–18:30 = 90p × 60k = 90,000
        // Tổng = 360,000
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 11, 30);
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 18, 30);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(3);
        assertThat(result.getTotalMinutes()).isEqualTo(420L); // 7 giờ = 420 phút

        // Segment 1: 11:30–12:00 = 30p × 40k
        BillingSegment s1 = result.getSegments().get(0);
        assertThat(s1.getDurationMinutes()).isEqualTo(30L);
        assertThat(s1.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(20_000));

        // Segment 2: 12:00–17:00 = 300p × 50k
        BillingSegment s2 = result.getSegments().get(1);
        assertThat(s2.getDurationMinutes()).isEqualTo(300L);
        assertThat(s2.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250_000));

        // Segment 3: 17:00–18:30 = 90p × 60k
        BillingSegment s3 = result.getSegments().get(2);
        assertThat(s3.getDurationMinutes()).isEqualTo(90L);
        assertThat(s3.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(90_000));

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(360_000));
    }

    // =========================================================================
    // Test 7 (Bonus): Overnight – phiên cross nửa đêm
    // =========================================================================

    @Test
    @DisplayName("Overnight – Pool ngày thường 22:00–23:00 = 60p × 70k = 70,000 VND")
    void testOvernightSegment() {
        // --- Arrange ---
        // 22:00–23:00 nằm trong rule overnight (22:00→08:00, 70k/h)
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 22, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 23, 0);
        Session session = sessionOf(start, end);

        when(priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(
                TableType.POOL, DayType.WEEKDAY))
                .thenReturn(weekdayRules());

        // --- Act ---
        BillingResult result = billingCalculator.calculate(session);

        // --- Assert ---
        assertThat(result.getSegments()).hasSize(1);
        assertThat(result.getTotalMinutes()).isEqualTo(60L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(70_000));
    }

    // =========================================================================
    // Test 8: resolveDayType
    // =========================================================================

    @Test
    @DisplayName("resolveDayType – Thứ Hai là WEEKDAY")
    void testResolveDayTypeWeekday() {
        LocalDateTime monday = LocalDateTime.of(2026, 3, 23, 10, 0);
        assertThat(billingCalculator.resolveDayType(monday)).isEqualTo(DayType.WEEKDAY);
    }

    @Test
    @DisplayName("resolveDayType – Chủ Nhật là WEEKEND")
    void testResolveDayTypeWeekend() {
        LocalDateTime sunday = LocalDateTime.of(2026, 3, 29, 10, 0);
        assertThat(billingCalculator.resolveDayType(sunday)).isEqualTo(DayType.WEEKEND);
    }

    @Test
    @DisplayName("resolveDayType – Thứ Tư + HOLIDAY_MODE = HOLIDAY")
    void testResolveDayTypeHoliday() {
        AppSetting setting = new AppSetting();
        setting.setSettingKey("HOLIDAY_MODE");
        setting.setSettingValue("TRUE");  // case-insensitive check
        when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.of(setting));

        LocalDateTime wednesday = LocalDateTime.of(2026, 3, 25, 14, 0);
        assertThat(billingCalculator.resolveDayType(wednesday)).isEqualTo(DayType.HOLIDAY);
    }
}
