package com.bida.controller.api;

import com.bida.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final com.bida.service.TelegramReportService telegramReportService;

    /**
     * GET /api/reports/kpis - KPI cards.
     */
    @GetMapping("/kpis")
    public ResponseEntity<?> getKPIs() {
        return ResponseEntity.ok(reportService.getKPIs());
    }

    /**
     * GET /api/reports/daily - Doanh thu 7 ngay gan nhat.
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getLast7DaysRevenue() {
        return ResponseEntity.ok(reportService.getLast7DaysRevenue());
    }

    /**
     * GET /api/reports/monthly - Doanh thu 12 thang.
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getLast12MonthsRevenue() {
        return ResponseEntity.ok(reportService.getLast12MonthsRevenue());
    }

    /**
     * GET /api/reports/tables?from=&to= - Thong ke ban.
     */
    @GetMapping("/tables")
    public ResponseEntity<?> getTableStats(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDT = from != null ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime toDT = to != null ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reportService.getTableUsageStats(fromDT, toDT));
    }

    /**
     * GET /api/reports/table-types - Doanh thu theo loai ban.
     */
    @GetMapping("/table-types")
    public ResponseEntity<?> getRevenueByTableType(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDT = from != null ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime toDT = to != null ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reportService.getRevenueByTableType(fromDT, toDT));
    }

    /**
     * GET /api/reports/staff - Bao cao nhan vien.
     */
    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xem hiệu suất nhân viên
    public ResponseEntity<?> getStaffPerformance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDT = from != null ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime toDT = to != null ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reportService.getStaffPerformance(fromDT, toDT));
    }

    /**
     * GET /api/reports/heatmap - Heatmap gio cao diem.
     */
    @GetMapping("/heatmap")
    public ResponseEntity<?> getHeatmap(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDT = from != null ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime toDT = to != null ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reportService.getHeatmapData(fromDT, toDT));
    }

    /**
     * GET /api/reports/products - Top san pham ban chay.
     */
    @GetMapping("/products")
    public ResponseEntity<?> getTopSellingProducts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDT = from != null ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime toDT = to != null ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reportService.getTopSellingProducts(fromDT, toDT));
    }

    /**
     * GET /api/reports/test-telegram - Kiểm tra kết nối Telegram Bot.
     */
    @GetMapping("/test-telegram")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN test Telegram bot
    public ResponseEntity<?> testTelegram() {
        try {
            telegramReportService.sendTestReport();
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi tin nhắn test tới Telegram của bạn!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
