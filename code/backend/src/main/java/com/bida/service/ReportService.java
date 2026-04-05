package com.bida.service;

import com.bida.repository.OrderItemRepository;
import com.bida.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final SessionRepository sessionRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Top san pham ban chay.
     */
    public List<Map<String, Object>> getTopSellingProducts(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = orderItemRepository.findTopSellingProducts(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row[0]);
            item.put("quantity", row[1]);
            item.put("revenue", row[2]);
            result.add(item);
        }
        return result;
    }

    /**
     * Doanh thu theo khoang thoi gian.
     */
    public BigDecimal getRevenue(LocalDateTime from, LocalDateTime to) {
        return sessionRepository.sumRevenueByDateRange(from, to);
    }

    /**
     * KPI cards: hom nay, tuan nay, thang nay + so sanh ky truoc.
     */
    public Map<String, Object> getKPIs() {
        Map<String, Object> kpis = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Hom nay
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        BigDecimal todayRevenue = getRevenue(todayStart, todayEnd);

        // Hom qua
        BigDecimal yesterdayRevenue = getRevenue(todayStart.minusDays(1), todayStart);
        kpis.put("todayRevenue", todayRevenue);
        kpis.put("todayChange", calcChangePercent(todayRevenue, yesterdayRevenue));

        // Tuan nay
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        BigDecimal weekRevenue = getRevenue(weekStart, todayEnd);
        BigDecimal lastWeekRevenue = getRevenue(weekStart.minusWeeks(1), weekStart);
        kpis.put("weekRevenue", weekRevenue);
        kpis.put("weekChange", calcChangePercent(weekRevenue, lastWeekRevenue));

        // Thang nay
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        BigDecimal monthRevenue = getRevenue(monthStart, todayEnd);
        BigDecimal lastMonthRevenue = getRevenue(monthStart.minusMonths(1), monthStart);
        kpis.put("monthRevenue", monthRevenue);
        kpis.put("monthChange", calcChangePercent(monthRevenue, lastMonthRevenue));

        return kpis;
    }

    /**
     * Doanh thu 7 ngay gan nhat.
     */
    public List<Map<String, Object>> getLast7DaysRevenue() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            BigDecimal revenue = getRevenue(from, to);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("revenue", revenue);
            result.add(item);
        }
        return result;
    }

    /**
     * Doanh thu 12 thang.
     */
    public List<Map<String, Object>> getLast12MonthsRevenue() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate month = today.minusMonths(i).withDayOfMonth(1);
            LocalDateTime from = month.atStartOfDay();
            LocalDateTime to = month.plusMonths(1).atStartOfDay();
            BigDecimal revenue = getRevenue(from, to);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month.toString().substring(0, 7)); // yyyy-MM
            item.put("revenue", revenue);
            result.add(item);
        }
        return result;
    }

    /**
     * Top ban duoc choi nhieu nhat.
     */
    public List<Map<String, Object>> getTableUsageStats(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = sessionRepository.findTableUsageStats(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tableId", row[0]);
            item.put("sessionCount", row[1]);
            item.put("totalRevenue", row[2]);
            result.add(item);
        }
        return result;
    }

    /**
     * Doanh thu theo loai ban.
     */
    public List<Map<String, Object>> getRevenueByTableType(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = sessionRepository.findRevenueByTableType(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tableType", row[0].toString());
            item.put("revenue", row[1]);
            result.add(item);
        }
        return result;
    }

    /**
     * Bao cao nhan vien.
     */
    public List<Map<String, Object>> getStaffPerformance(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = sessionRepository.findStaffPerformanceStats(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("staffId", row[0]);
            item.put("staffName", row[1]);
            item.put("sessionCount", row[2]);
            item.put("totalRevenue", row[3]);
            result.add(item);
        }
        return result;
    }

    /**
     * Heatmap data: dayOfWeek x hour → count.
     */
    public List<Map<String, Object>> getHeatmapData(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = sessionRepository.findHeatmapData(from, to);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dayOfWeek", row[0]);
            item.put("hour", row[1]);
            item.put("count", row[2]);
            result.add(item);
        }
        return result;
    }

    private BigDecimal calcChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }
}
