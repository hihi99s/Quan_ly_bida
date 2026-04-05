package com.bida.billing;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.service.BillingCalculator;
import com.bida.billing.strategy.PricingStrategy;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho tính năng chuyển bàn (Phase 3) trong {@link BillingCalculator}.
 *
 * <h3>Kịch bản phải đúng:</h3>
 * <pre>
 *   Phiên bình thường (không chuyển):   toàn bộ thời gian × rate của bàn duy nhất
 *   Phiên có chuyển bàn:
 *     Phase 1: startTime → transferredAt  × rate của bàn GỐC (originalTableType)
 *     Phase 2: transferredAt → endTime    × rate của bàn MỚI (session.table.tableType)
 *     Total   = Phase1 + Phase2
 * </pre>
 *
 * <h3>Bảng giá giả định trong các test:</h3>
 * <pre>
 *   POOL  / WEEKDAY → 50,000 VND/h
 *   VIP   / WEEKDAY → 100,000 VND/h
 *   CAROM / WEEKDAY → 70,000 VND/h
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingCalculator – Chuyển Bàn (Transfer Table)")
class BillingCalculatorTransferTest {

    // =========================================================================
    // Mocks
    // =========================================================================

    @Mock private PricingStrategy pricingStrategy;
    @Mock private AppSettingRepository appSettingRepository;
    @Mock private HolidayCalendarRepository holidayCalendarRepository;

    @InjectMocks
    private BillingCalculator billingCalculator;

    // =========================================================================
    // Setup
    // =========================================================================

    @BeforeEach
    void setUp() {
        // Mặc định: không có holiday mode
        lenient().when(appSettingRepository.findBySettingKey("HOLIDAY_MODE"))
                .thenReturn(Optional.empty());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Tạo BillingResult mock với tổng tiền cụ thể.
     * Dùng để stub pricingStrategy.calculate().
     */
    private BillingResult billingOf(long amount) {
        return BillingResult.builder()
                .totalAmount(BigDecimal.valueOf(amount))
                .segments(Collections.emptyList())
                .totalMinutes(60L)
                .build();
    }

    /**
     * Tạo Session BAN THƯỜNG — không có chuyển bàn.
     *
     * @param tableType loại bàn duy nhất
     * @param start     giờ bắt đầu
     * @param end       giờ kết thúc
     */
    private Session normalSession(TableType tableType,
                                  LocalDateTime start, LocalDateTime end) {
        BilliardTable table = new BilliardTable();
        table.setTableType(tableType);
        table.setName("Ban Test");

        Session session = new Session();
        session.setId(1L);
        session.setTable(table);
        session.setStartTime(start);
        session.setEndTime(end);
        // transferredAt = null  →  không chuyển bàn
        return session;
    }

    /**
     * Tạo Session ĐÃ CHUYỂN BÀN.
     *
     * @param originalType  loại bàn gốc (trước khi chuyển)
     * @param newType       loại bàn mới (bàn hiện tại sau khi chuyển)
     * @param start         giờ bắt đầu phiên
     * @param transferredAt giờ chuyển bàn
     * @param end           giờ kết thúc phiên (null nếu phiên đang chạy)
     */
    private Session transferSession(TableType originalType, TableType newType,
                                    LocalDateTime start,
                                    LocalDateTime transferredAt,
                                    LocalDateTime end) {
        BilliardTable newTable = new BilliardTable();
        newTable.setTableType(newType);
        newTable.setName("Ban Moi");

        Session session = new Session();
        session.setId(2L);
        session.setTable(newTable);          // bàn hiện tại = bàn MỚI
        session.setStartTime(start);
        session.setEndTime(end);
        session.setOriginalTableType(originalType); // ghi nhận bàn GỐC
        session.setTransferredAt(transferredAt);
        return session;
    }

    // =========================================================================
    // TC-01: Phiên không chuyển bàn — code path cũ hoạt động bình thường
    // =========================================================================

    @Test
    @DisplayName("TC-01: Không chuyển bàn — tính theo 1 rate duy nhất")
    void tc01_noTransfer_usesOriginalPath() {
        // Arrange
        // Pool 20:00 → 21:00 (1 tiếng, không chuyển)
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 20, 0); // Thứ Tư
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 21, 0);
        Session session = normalSession(TableType.POOL, start, end);

        when(pricingStrategy.calculate(start, end, TableType.POOL, DayType.WEEKDAY))
                .thenReturn(billingOf(50_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert
        assertThat(result.getTotalAmount()).isEqualByComparingTo("50000");

        // Verify: chỉ gọi POOL — không gọi VIP hay CAROM
        verify(pricingStrategy, times(1))
                .calculate(any(), any(), eq(TableType.POOL), any());
        verify(pricingStrategy, never())
                .calculate(any(), any(), eq(TableType.VIP), any());
    }

    // =========================================================================
    // TC-02: Chuyển bàn đơn giản Pool → VIP, cùng ngày, cùng khung giờ
    // =========================================================================

    @Test
    @DisplayName("TC-02: Pool→VIP cùng ngày | Phase1=50k + Phase2=100k = 150k")
    void tc02_transfer_PoolToVip_sameDay() {
        // Arrange
        // Pool 01: 20:00 bắt đầu
        // 21:00: chuyển sang VIP 01
        // 22:00: kết thúc
        //
        // Kỳ vọng:
        //   Phase 1 (Pool, 20:00→21:00) = 50,000
        //   Phase 2 (VIP,  21:00→22:00) = 100,000
        //   Tổng = 150,000
        LocalDateTime start       = LocalDateTime.of(2026, 3, 25, 20, 0);
        LocalDateTime transferAt  = LocalDateTime.of(2026, 3, 25, 21, 0);
        LocalDateTime end         = LocalDateTime.of(2026, 3, 25, 22, 0);

        Session session = transferSession(TableType.POOL, TableType.VIP,
                start, transferAt, end);

        // Phase 1: Pool 20:00→21:00
        when(pricingStrategy.calculate(start, transferAt, TableType.POOL, DayType.WEEKDAY))
                .thenReturn(billingOf(50_000));
        // Phase 2: VIP 21:00→22:00
        when(pricingStrategy.calculate(transferAt, end, TableType.VIP, DayType.WEEKDAY))
                .thenReturn(billingOf(100_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert
        assertThat(result.getTotalAmount())
                .as("Tổng tiền phải bằng Phase1 + Phase2")
                .isEqualByComparingTo("150000");

        // Verify mỗi phase gọi đúng tableType
        verify(pricingStrategy).calculate(start, transferAt, TableType.POOL, DayType.WEEKDAY);
        verify(pricingStrategy).calculate(transferAt, end,   TableType.VIP,  DayType.WEEKDAY);
    }

    // =========================================================================
    // TC-03: Pool → CAROM — kiểm tra rate CAROM được dùng ở Phase 2
    // =========================================================================

    @Test
    @DisplayName("TC-03: Pool→Carom | Phase1=25k (30p) + Phase2=35k (30p) = 60k")
    void tc03_transfer_PoolToCarom() {
        // Arrange
        // 10:00 bắt đầu Pool, 10:30 chuyển Carom, 11:00 kết thúc
        LocalDateTime start      = LocalDateTime.of(2026, 3, 25, 10, 0);
        LocalDateTime transferAt = LocalDateTime.of(2026, 3, 25, 10, 30);
        LocalDateTime end        = LocalDateTime.of(2026, 3, 25, 11, 0);

        Session session = transferSession(TableType.POOL, TableType.CAROM,
                start, transferAt, end);

        when(pricingStrategy.calculate(start, transferAt, TableType.POOL,  DayType.WEEKDAY))
                .thenReturn(billingOf(25_000));
        when(pricingStrategy.calculate(transferAt, end, TableType.CAROM, DayType.WEEKDAY))
                .thenReturn(billingOf(35_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert
        assertThat(result.getTotalAmount()).isEqualByComparingTo("60000");

        verify(pricingStrategy).calculate(any(), any(), eq(TableType.POOL),  eq(DayType.WEEKDAY));
        verify(pricingStrategy).calculate(any(), any(), eq(TableType.CAROM), eq(DayType.WEEKDAY));
        // VIP không được gọi
        verify(pricingStrategy, never()).calculate(any(), any(), eq(TableType.VIP), any());
    }

    // =========================================================================
    // TC-04: Chuyển bàn qua giới hạn 00:00 (phase 2 cross midnight)
    // =========================================================================

    @Test
    @DisplayName("TC-04: Chuyển bàn qua 00:00 | Phase1=60k + Phase2(đêm=40k+sáng=50k) = 150k")
    void tc04_transfer_crossMidnight() {
        // Arrange
        // Pool: Thứ 6, 23:00 bắt đầu
        // 23:30: chuyển sang VIP
        // Thứ 7, 01:00: kết thúc (sau nửa đêm)
        //
        // Phase 1: Pool [Fri 23:00→23:30] → 1 sub-range (WEEKDAY) → 60,000
        // Phase 2: VIP  [Fri 23:30→Sat 00:00] (WEEKDAY) → 40,000
        //          VIP  [Sat 00:00→Sat 01:00] (WEEKEND) → 50,000
        //                                     Phase 2 tổng → 90,000
        // Tổng phiên: 60,000 + 90,000 = 150,000

        LocalDateTime start       = LocalDateTime.of(2026, 3, 27, 23, 0); // Thứ 6
        LocalDateTime transferAt  = LocalDateTime.of(2026, 3, 27, 23, 30);
        LocalDateTime midnight    = LocalDateTime.of(2026, 3, 28, 0, 0);   // Thứ 7
        LocalDateTime end         = LocalDateTime.of(2026, 3, 28, 1, 0);

        Session session = transferSession(TableType.POOL, TableType.VIP,
                start, transferAt, end);

        // Phase 1: Pool WEEKDAY
        when(pricingStrategy.calculate(start, transferAt, TableType.POOL, DayType.WEEKDAY))
                .thenReturn(billingOf(60_000));
        // Phase 2 sub-range 1: VIP Fri→Sat midnight (WEEKDAY)
        when(pricingStrategy.calculate(transferAt, midnight, TableType.VIP, DayType.WEEKDAY))
                .thenReturn(billingOf(40_000));
        // Phase 2 sub-range 2: VIP Sat morning (WEEKEND)
        when(pricingStrategy.calculate(midnight, end, TableType.VIP, DayType.WEEKEND))
                .thenReturn(billingOf(50_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert
        assertThat(result.getTotalAmount())
                .as("Phase1(60k) + Phase2(40k+50k) = 150,000")
                .isEqualByComparingTo("150000");

        // Phase 1: 1 sub-range với POOL
        verify(pricingStrategy).calculate(start, transferAt, TableType.POOL, DayType.WEEKDAY);
        // Phase 2: 2 sub-ranges với VIP, DayType khác nhau
        verify(pricingStrategy).calculate(transferAt, midnight, TableType.VIP, DayType.WEEKDAY);
        verify(pricingStrategy).calculate(midnight,   end,      TableType.VIP, DayType.WEEKEND);
    }

    // =========================================================================
    // TC-05: calculateCurrentAmount — phiên đang chạy, đã chuyển bàn
    // =========================================================================

    @Test
    @DisplayName("TC-05: calculateCurrentAmount — Pool→VIP đang chơi | Preview = Phase1 + Phase2-đến-hiện-tại")
    void tc05_currentAmount_withTransfer() {
        // Arrange
        // Phiên đang chạy (endTime = null)
        // Pool: 20:00 bắt đầu
        // 21:00: chuyển sang VIP
        // "Bây giờ": ~22:00 (test chạy trong vùng cùng ngày, không cần giá trị chính xác)
        LocalDateTime start      = LocalDateTime.of(2026, 3, 25, 20, 0);
        LocalDateTime transferAt = LocalDateTime.of(2026, 3, 25, 21, 0);

        Session session = transferSession(TableType.POOL, TableType.VIP,
                start, transferAt, null); // endTime = null → đang chạy

        // Stub tất cả sub-range (any time range) cho Pool và VIP
        when(pricingStrategy.calculate(
                eq(start), eq(transferAt), eq(TableType.POOL), any()))
                .thenReturn(billingOf(50_000));
        when(pricingStrategy.calculate(
                eq(transferAt), any(LocalDateTime.class), eq(TableType.VIP), any()))
                .thenReturn(billingOf(80_000));

        // Act
        BigDecimal current = billingCalculator.calculateCurrentAmount(session);

        // Assert: total = Phase1(50k) + Phase2 preview(80k) = 130k
        assertThat(current)
                .as("Preview = Pool phase(50k) + VIP phase(80k) = 130,000")
                .isEqualByComparingTo("130000");

        // Phase 1 (Pool, đã cố định)
        verify(pricingStrategy).calculate(eq(start), eq(transferAt), eq(TableType.POOL), any());
        // Phase 2 (VIP, đến "now")
        verify(pricingStrategy).calculate(eq(transferAt), any(), eq(TableType.VIP), any());
    }

    // =========================================================================
    // TC-06: calculateCurrentAmount — phiên đang chạy KHÔNG chuyển bàn
    // =========================================================================

    @Test
    @DisplayName("TC-06: calculateCurrentAmount — bình thường (không chuyển) | Preview đúng theo 1 bàn")
    void tc06_currentAmount_noTransfer() {
        // Arrange — chỉ dùng Pool, không transfer
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 20, 0);

        Session session = normalSession(TableType.POOL, start, null); // đang chạy

        when(pricingStrategy.calculate(eq(start), any(LocalDateTime.class),
                eq(TableType.POOL), any()))
                .thenReturn(billingOf(70_000));

        // Act
        BigDecimal current = billingCalculator.calculateCurrentAmount(session);

        // Assert
        assertThat(current).isEqualByComparingTo("70000");

        // Verify: VIP hoàn toàn không được gọi
        verify(pricingStrategy, never()).calculate(any(), any(), eq(TableType.VIP), any());
    }

    // =========================================================================
    // TC-07: Phase 1 rất ngắn — chuyển bàn ngay sau khi bắt đầu (1 phút)
    // =========================================================================

    @Test
    @DisplayName("TC-07: Chuyển bàn ngay sau 1 phút | Phase1≈833đ + Phase2=100k ≈ 100,833đ")
    void tc07_transfer_phase1VeryShort() {
        // Arrange
        // Pool 10:00:00 → chuyển VIP 10:01:00 → kết thúc 11:01:00
        // Phase 1: 1 phút Pool ≈ 1 * 50,000 / 60 = 834 VND (làm tròn)
        // Phase 2: 60 phút VIP = 100,000 VND
        LocalDateTime start      = LocalDateTime.of(2026, 3, 25, 10, 0);
        LocalDateTime transferAt = LocalDateTime.of(2026, 3, 25, 10, 1);
        LocalDateTime end        = LocalDateTime.of(2026, 3, 25, 11, 1);

        Session session = transferSession(TableType.POOL, TableType.VIP,
                start, transferAt, end);

        when(pricingStrategy.calculate(start, transferAt, TableType.POOL, DayType.WEEKDAY))
                .thenReturn(billingOf(834));
        when(pricingStrategy.calculate(transferAt, end, TableType.VIP, DayType.WEEKDAY))
                .thenReturn(billingOf(100_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert
        assertThat(result.getTotalAmount()).isEqualByComparingTo("100834");
        assertThat(result.getTotalAmount())
                .as("Tổng phải lớn hơn Phase 2 (vì có thêm Phase 1)")
                .isGreaterThan(BigDecimal.valueOf(100_000));
    }

    // =========================================================================
    // TC-08: Kiểm tra độc lập — originalTableType null không crash
    // =========================================================================

    @Test
    @DisplayName("TC-08: transferredAt có giá trị nhưng originalTableType = null → fallback path cũ")
    void tc08_transferredAtSet_butOriginalTypeNull_fallsBackToNormalPath() {
        // Đây là guard case: nếu DB có transferredAt nhưng originalTableType bị null
        // (dữ liệu cũ hoặc lỗi partial save) → phải fallback, không crash.
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 20, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 22, 0);

        BilliardTable table = new BilliardTable();
        table.setTableType(TableType.VIP);

        Session session = new Session();
        session.setId(99L);
        session.setTable(table);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setTransferredAt(LocalDateTime.of(2026, 3, 25, 21, 0));
        session.setOriginalTableType(null); // null → fallback

        when(pricingStrategy.calculate(start, end, TableType.VIP, DayType.WEEKDAY))
                .thenReturn(billingOf(200_000));

        // Act — không được throw exception
        assertThatCode(() -> billingCalculator.calculate(session))
                .doesNotThrowAnyException();

        BillingResult result = billingCalculator.calculate(session);
        // Fallback: tính toàn bộ theo VIP
        assertThat(result.getTotalAmount()).isEqualByComparingTo("200000");
    }

    // =========================================================================
    // TC-09: Verify originalTableType KHÔNG bị dùng cho phiên bình thường
    // =========================================================================

    @Test
    @DisplayName("TC-09: Phiên không có transfer — originalTableType bị bỏ qua hoàn toàn")
    void tc09_noTransfer_originalTableTypeIgnored() {
        // Arrange — session không có transferredAt (bình thường)
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 15, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 3, 25, 16, 0);

        Session session = normalSession(TableType.CAROM, start, end);
        // Giả sử có field originalTableType được set nhưng transferredAt = null
        // (không nên xảy ra, nhưng test phòng thủ)
        session.setOriginalTableType(TableType.POOL);
        // transferredAt vẫn null → không trigger transfer path

        when(pricingStrategy.calculate(start, end, TableType.CAROM, DayType.WEEKDAY))
                .thenReturn(billingOf(70_000));

        // Act
        BillingResult result = billingCalculator.calculate(session);

        // Assert: chạy theo path bình thường (CAROM), originalTableType (POOL) bị bỏ qua
        assertThat(result.getTotalAmount()).isEqualByComparingTo("70000");

        verify(pricingStrategy).calculate(any(), any(), eq(TableType.CAROM), any());
        verify(pricingStrategy, never()).calculate(any(), any(), eq(TableType.POOL), any());
    }

    // =========================================================================
    // TC-10: endTime = null → IllegalArgumentException (giữ nguyên behavior cũ)
    // =========================================================================

    @Test
    @DisplayName("TC-10: calculate() với endTime = null → IllegalArgumentException")
    void tc10_nullEndTime_throwsException() {
        // Session chưa kết thúc (endTime = null) không thể tính tiền chính thức
        Session session = normalSession(TableType.POOL,
                LocalDateTime.of(2026, 3, 25, 10, 0), null);

        assertThatThrownBy(() -> billingCalculator.calculate(session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chua co endTime");
    }
}
