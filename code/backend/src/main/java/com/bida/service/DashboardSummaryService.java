package com.bida.service;

import com.bida.entity.BilliardTable;
import com.bida.entity.Customer;
import com.bida.entity.Reservation;
import com.bida.entity.StaffSchedule;
import com.bida.entity.enums.MembershipTier;
import com.bida.entity.enums.ReservationStatus;
import com.bida.entity.enums.TableStatus;
import com.bida.repository.CustomerRepository;
import com.bida.repository.InvoiceRepository;
import com.bida.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tổng hợp dữ liệu Dashboard.
 *
 * Cung cấp nhiều API tổng hợp giúp frontend gọi ít request nhất có thể:
 * - summary:       Tổng quan real-time (bàn, doanh thu, hóa đơn)
 * - kpis:          KPI so sánh hôm nay / tuần / tháng với kỳ trước
 * - revenueChart:  Biểu đồ doanh thu 7 ngày + 12 tháng
 * - topProducts:   Top sản phẩm bán chạy
 * - topCustomers:  Top khách VIP
 * - customerStats: Phân bổ khách theo hạng thành viên
 * - reservations:  Đặt bàn đang chờ & sắp tới
 * - staffToday:    Tình hình nhân viên hôm nay
 * - lowStock:      Cảnh báo sản phẩm sắp hết hàng
 * - fullDashboard: Gom TẤT CẢ vào 1 request duy nhất
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSummaryService {

    private final TableService tableService;
    private final ReportService reportService;
    private final InvoiceRepository invoiceRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final ProductService productService;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final StaffScheduleService staffScheduleService;

    /* ───────────────────────────────────────────────
     * 1. SUMMARY - Tổng quan hệ thống (giữ nguyên logic cũ)
     * ─────────────────────────────────────────────── */

    /**
     * Trả về tổng quan hệ thống tại thời điểm hiện tại.
     *
     * - totalTables / tablesPlaying / tablesPaused / tablesAvailable / tablesReserved / tablesMaintenance
     * - revenueToday, ordersToday
     * - pendingReservations
     * - lateCheckIns
     */
    public Map<String, Object> getSummary() {
        // 1. Lấy danh sách bàn
        List<BilliardTable> allTables = tableService.getAllTables().stream()
                .filter(t -> t.getStatus() != TableStatus.DISABLED)
                .toList();

        int totalTables = allTables.size();
        int tablesPlaying = (int) allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.PLAYING).count();
        int tablesPaused = (int) allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.PAUSED).count();
        int tablesAvailable = (int) allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.AVAILABLE).count();
        int tablesReserved = (int) allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.RESERVED).count();
        int tablesMaintenance = (int) allTables.stream()
                .filter(t -> t.getStatus() == TableStatus.MAINTENANCE).count();

        // 2. Doanh thu hôm nay
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        BigDecimal revenueToday = reportService.getRevenue(todayStart, todayEnd);
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;

        // 3. Số hóa đơn hôm nay
        long ordersToday = invoiceRepository.countByDateRange(todayStart, todayEnd);

        // 4. Đặt bàn đang chờ
        int pendingReservations = reservationService.getPendingReservations().size();

        // 5. Nhân viên trễ check-in
        int lateCheckIns = staffScheduleService.getLateCheckIns().size();

        // Build response
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTables", totalTables);
        summary.put("tablesPlaying", tablesPlaying);
        summary.put("tablesPaused", tablesPaused);
        summary.put("tablesAvailable", tablesAvailable);
        summary.put("tablesReserved", tablesReserved);
        summary.put("tablesMaintenance", tablesMaintenance);
        summary.put("revenueToday", revenueToday);
        summary.put("ordersToday", ordersToday);
        summary.put("pendingReservations", pendingReservations);
        summary.put("lateCheckIns", lateCheckIns);

        log.debug("Dashboard Summary: {} bàn, {} đang chơi, doanh thu {} VND, {} hóa đơn",
                totalTables, tablesPlaying, revenueToday, ordersToday);

        return summary;
    }

    /* ───────────────────────────────────────────────
     * 2. KPIs - So sánh doanh thu với kỳ trước
     * ─────────────────────────────────────────────── */

    /**
     * KPI cards: hôm nay, tuần này, tháng này + % thay đổi so với kỳ trước.
     * Delegate sang ReportService.getKPIs().
     */
    public Map<String, Object> getKpis() {
        return reportService.getKPIs();
    }

    /* ───────────────────────────────────────────────
     * 3. REVENUE CHART - Biểu đồ doanh thu
     * ─────────────────────────────────────────────── */

    /**
     * Trả về dữ liệu biểu đồ doanh thu:
     * - daily:   7 ngày gần nhất  [{date, revenue}]
     * - monthly: 12 tháng gần nhất [{month, revenue}]
     */
    public Map<String, Object> getRevenueChart() {
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("daily", reportService.getLast7DaysRevenue());
        chart.put("monthly", reportService.getLast12MonthsRevenue());
        return chart;
    }

    /* ───────────────────────────────────────────────
     * 4. TOP PRODUCTS - Sản phẩm bán chạy hôm nay
     * ─────────────────────────────────────────────── */

    /**
     * Top sản phẩm bán chạy trong khoảng thời gian (mặc định: hôm nay).
     * [{name, quantity, revenue}]
     */
    public List<Map<String, Object>> getTopProducts(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            from = LocalDate.now().atStartOfDay();
            to = from.plusDays(1);
        }
        return reportService.getTopSellingProducts(from, to);
    }

    /* ───────────────────────────────────────────────
     * 5. TOP CUSTOMERS - Khách VIP chi tiêu nhiều nhất
     * ─────────────────────────────────────────────── */

    /**
     * Top 10 khách hàng VIP theo tổng chi tiêu.
     * [{id, name, phone, membershipTier, totalSpent, points}]
     */
    public List<Map<String, Object>> getTopCustomers() {
        List<Customer> vips = customerService.getTopVIP();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Customer c : vips) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("phone", c.getPhone());
            item.put("membershipTier", c.getMembershipTier().name());
            item.put("totalSpent", c.getTotalSpent());
            item.put("points", c.getPoints());
            result.add(item);
        }
        return result;
    }

    /* ───────────────────────────────────────────────
     * 6. CUSTOMER STATS - Phân bổ khách theo hạng thành viên
     * ─────────────────────────────────────────────── */

    /**
     * Thống kê phân bổ khách hàng theo membership tier.
     * {totalCustomers, tiers: [{tier, count, percentage}]}
     */
    public Map<String, Object> getCustomerStats() {
        List<Customer> allCustomers = customerService.getAllCustomers();
        int total = allCustomers.size();

        Map<MembershipTier, Long> tierCounts = allCustomers.stream()
                .collect(Collectors.groupingBy(Customer::getMembershipTier, Collectors.counting()));

        List<Map<String, Object>> tiers = new ArrayList<>();
        for (MembershipTier tier : MembershipTier.values()) {
            long count = tierCounts.getOrDefault(tier, 0L);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tier", tier.name());
            item.put("count", count);
            item.put("percentage", total > 0 ? Math.round(count * 100.0 / total) : 0);
            tiers.add(item);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCustomers", total);
        stats.put("tiers", tiers);
        return stats;
    }

    /* ───────────────────────────────────────────────
     * 7. RESERVATIONS - Đặt bàn đang chờ & sắp tới
     * ─────────────────────────────────────────────── */

    /**
     * Danh sách đặt bàn đang PENDING + đặt bàn CONFIRMED sắp tới trong 24h.
     * {pending: [...], upcoming: [...]}
     */
    public Map<String, Object> getReservationOverview() {
        // Pending
        List<Reservation> pending = reservationService.getPendingReservations();
        List<Map<String, Object>> pendingList = pending.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());

        // Upcoming confirmed trong 24h tới
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);
        List<Reservation> upcoming = reservationRepository
                .findByReservedTimeBetweenAndStatus(now, next24h, ReservationStatus.CONFIRMED);
        List<Map<String, Object>> upcomingList = upcoming.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pendingCount", pendingList.size());
        result.put("upcomingCount", upcomingList.size());
        result.put("pending", pendingList);
        result.put("upcoming", upcomingList);
        return result;
    }

    private Map<String, Object> mapReservation(Reservation r) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", r.getId());
        item.put("tableName", r.getTable().getName());
        item.put("customerName", r.getCustomerName());
        item.put("customerPhone", r.getCustomerPhone());
        item.put("reservedTime", r.getReservedTime().toString());
        item.put("durationMinutes", r.getDurationMinutes());
        item.put("status", r.getStatus().name());
        item.put("note", r.getNote());
        return item;
    }

    /* ───────────────────────────────────────────────
     * 8. STAFF TODAY - Tình hình nhân viên hôm nay
     * ─────────────────────────────────────────────── */

    /**
     * Tình hình nhân viên hôm nay:
     * - totalScheduled: Tổng nhân viên có lịch
     * - checkedIn: Đã check-in
     * - lateCheckIns: Chưa check-in nhưng đã đến giờ
     * - schedules: Chi tiết từng nhân viên
     */
    public Map<String, Object> getStaffToday() {
        LocalDate today = LocalDate.now();
        List<StaffSchedule> schedules = staffScheduleService.getSchedulesByDate(today);
        List<StaffSchedule> lateList = staffScheduleService.getLateCheckIns();

        long checkedIn = schedules.stream()
                .filter(s -> s.getCheckInTime() != null)
                .count();

        List<Map<String, Object>> scheduleDetails = schedules.stream().map(s -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("staffName", s.getUser().getFullName());
            item.put("shiftName", s.getShift().getName());
            item.put("shiftStart", s.getShift().getStartTime().toString());
            item.put("shiftEnd", s.getShift().getEndTime().toString());
            item.put("status", s.getStatus().name());
            item.put("checkInTime", s.getCheckInTime() != null ? s.getCheckInTime().toString() : null);
            item.put("checkOutTime", s.getCheckOutTime() != null ? s.getCheckOutTime().toString() : null);
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalScheduled", schedules.size());
        result.put("checkedIn", checkedIn);
        result.put("lateCheckIns", lateList.size());
        result.put("schedules", scheduleDetails);
        return result;
    }

    /* ───────────────────────────────────────────────
     * 9. LOW STOCK - Cảnh báo sản phẩm sắp hết hàng
     * ─────────────────────────────────────────────── */

    /**
     * Danh sách sản phẩm có tồn kho < 5 (cần nhập thêm).
     * [{id, name, category, stockQuantity, price}]
     */
    public List<Map<String, Object>> getLowStockAlerts() {
        return productService.getLowStockProducts().stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("category", p.getCategory().name());
            item.put("stockQuantity", p.getStockQuantity());
            item.put("price", p.getPrice());
            return item;
        }).collect(Collectors.toList());
    }

    /* ───────────────────────────────────────────────
     * 10. TABLE ANALYTICS - Thống kê sử dụng bàn
     * ─────────────────────────────────────────────── */

    /**
     * Phân tích sử dụng bàn & doanh thu theo loại bàn (tháng này).
     * {tableUsage: [...], revenueByType: [...]}
     */
    public Map<String, Object> getTableAnalytics() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("tableUsage", reportService.getTableUsageStats(monthStart, now));
        analytics.put("revenueByType", reportService.getRevenueByTableType(monthStart, now));
        return analytics;
    }

    /* ───────────────────────────────────────────────
     * 11. FULL DASHBOARD - Gom tất cả vào 1 request
     * ─────────────────────────────────────────────── */

    /**
     * Gom TẤT CẢ dữ liệu dashboard vào 1 response duy nhất.
     * Frontend chỉ cần gọi 1 API duy nhất để render toàn bộ dashboard.
     *
     * {
     *   summary: {...},
     *   kpis: {...},
     *   revenueChart: {daily: [...], monthly: [...]},
     *   topProducts: [...],
     *   topCustomers: [...],
     *   customerStats: {...},
     *   reservations: {...},
     *   staffToday: {...},
     *   lowStockAlerts: [...],
     *   tableAnalytics: {...}
     * }
     */
    public Map<String, Object> getFullDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("summary", getSummary());
        dashboard.put("kpis", getKpis());
        dashboard.put("revenueChart", getRevenueChart());
        dashboard.put("topProducts", getTopProducts(null, null));
        dashboard.put("topCustomers", getTopCustomers());
        dashboard.put("customerStats", getCustomerStats());
        dashboard.put("reservations", getReservationOverview());
        dashboard.put("staffToday", getStaffToday());
        dashboard.put("lowStockAlerts", getLowStockAlerts());
        dashboard.put("tableAnalytics", getTableAnalytics());

        log.info("Full Dashboard loaded successfully");
        return dashboard;
    }
}
