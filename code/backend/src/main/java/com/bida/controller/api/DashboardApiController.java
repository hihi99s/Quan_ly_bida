package com.bida.controller.api;

import com.bida.service.DashboardSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API - Dashboard tổng hợp.
 *
 * Cung cấp 11 endpoint cho frontend hiển thị dashboard:
 *
 *  GET /api/dashboard/summary          → Tổng quan hệ thống (bàn, doanh thu, hóa đơn)
 *  GET /api/dashboard/kpis             → KPI cards (hôm nay / tuần / tháng + % thay đổi)
 *  GET /api/dashboard/revenue-chart    → Biểu đồ doanh thu (7 ngày + 12 tháng)
 *  GET /api/dashboard/top-products     → Top sản phẩm bán chạy
 *  GET /api/dashboard/top-customers    → Top 10 khách VIP
 *  GET /api/dashboard/customer-stats   → Phân bổ khách theo hạng thành viên
 *  GET /api/dashboard/reservations     → Đặt bàn đang chờ & sắp tới
 *  GET /api/dashboard/staff-today      → Tình hình nhân viên hôm nay
 *  GET /api/dashboard/low-stock        → Cảnh báo sản phẩm sắp hết hàng
 *  GET /api/dashboard/table-analytics  → Thống kê sử dụng bàn & doanh thu theo loại
 *  GET /api/dashboard/full             → Gom TẤT CẢ vào 1 response duy nhất
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardSummaryService dashboardSummaryService;

    /* ─── 1. Tổng quan hệ thống ─── */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardSummaryService.getSummary());
    }

    /* ─── 2. KPI cards ─── */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        return ResponseEntity.ok(dashboardSummaryService.getKpis());
    }

    /* ─── 3. Biểu đồ doanh thu ─── */
    @GetMapping("/revenue-chart")
    public ResponseEntity<Map<String, Object>> getRevenueChart() {
        return ResponseEntity.ok(dashboardSummaryService.getRevenueChart());
    }

    /* ─── 4. Top sản phẩm bán chạy ─── */
    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(dashboardSummaryService.getTopProducts(from, to));
    }

    /* ─── 5. Top 10 khách VIP ─── */
    @GetMapping("/top-customers")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers() {
        return ResponseEntity.ok(dashboardSummaryService.getTopCustomers());
    }

    /* ─── 6. Phân bổ khách theo hạng ─── */
    @GetMapping("/customer-stats")
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        return ResponseEntity.ok(dashboardSummaryService.getCustomerStats());
    }

    /* ─── 7. Đặt bàn đang chờ & sắp tới ─── */
    @GetMapping("/reservations")
    public ResponseEntity<Map<String, Object>> getReservations() {
        return ResponseEntity.ok(dashboardSummaryService.getReservationOverview());
    }

    /* ─── 8. Tình hình nhân viên hôm nay ─── */
    @GetMapping("/staff-today")
    public ResponseEntity<Map<String, Object>> getStaffToday() {
        return ResponseEntity.ok(dashboardSummaryService.getStaffToday());
    }

    /* ─── 9. Cảnh báo sản phẩm sắp hết hàng ─── */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Map<String, Object>>> getLowStock() {
        return ResponseEntity.ok(dashboardSummaryService.getLowStockAlerts());
    }

    /* ─── 10. Thống kê sử dụng bàn ─── */
    @GetMapping("/table-analytics")
    public ResponseEntity<Map<String, Object>> getTableAnalytics() {
        return ResponseEntity.ok(dashboardSummaryService.getTableAnalytics());
    }

    /* ─── 11. FULL - Gom tất cả vào 1 request ─── */
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> getFullDashboard() {
        return ResponseEntity.ok(dashboardSummaryService.getFullDashboard());
    }
}
