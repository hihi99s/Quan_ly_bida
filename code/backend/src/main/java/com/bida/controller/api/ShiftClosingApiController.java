package com.bida.controller.api;

import com.bida.entity.ShiftClosing;
import com.bida.service.ShiftClosingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API chốt ca (Shift Closing).
 *
 * POST /api/staff/close-shift — Nhân viên chốt ca, nhập tiền thực tế.
 * GET  /api/staff/shift-closings — Lấy danh sách chốt ca (có filter theo ngày).
 */
@Slf4j
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class ShiftClosingApiController {

    private final ShiftClosingService shiftClosingService;

    /**
     * POST /api/staff/close-shift
     *
     * Body: { "shiftId": 1, "actualCash": 500000, "note": "optional" }
     */
    @PostMapping("/close-shift")
    public ResponseEntity<?> closeShift(@RequestBody Map<String, Object> body,
                                         Principal principal) {
        try {
            // Parse request body
            Long shiftId = Long.valueOf(body.get("shiftId").toString());
            BigDecimal actualCash = new BigDecimal(body.get("actualCash").toString());
            String note = body.get("note") != null ? body.get("note").toString() : null;

            // Lấy username từ security context
            String username = principal.getName();

            // Gọi service chốt ca
            ShiftClosing closing = shiftClosingService.closeShift(username, shiftId, actualCash, note);

            // Build response
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", closing.getId());
            data.put("staffName", closing.getStaff().getFullName());
            data.put("shiftName", closing.getShift().getName());
            data.put("systemRevenue", closing.getSystemRevenue());
            data.put("actualCash", closing.getActualCash());
            data.put("discrepancy", closing.getDiscrepancy());
            data.put("note", closing.getNote());
            data.put("closingTime", closing.getClosingTime().toString());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Chốt ca thành công!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi chốt ca: {}", e.getMessage(), e);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi hệ thống.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * GET /api/staff/shift-closings?from=2026-04-01&to=2026-04-03
     *
     * Lấy danh sách chốt ca, filter theo khoảng ngày (tùy chọn).
     */
    @GetMapping("/shift-closings")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xem toàn bộ lịch sử chốt ca
    public ResponseEntity<?> getShiftClosings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        try {
            List<ShiftClosing> closings;

            if (from != null && to != null) {
                LocalDateTime fromDt = from.atStartOfDay();
                LocalDateTime toDt = to.plusDays(1).atStartOfDay();
                closings = shiftClosingService.getClosingsByDateRange(fromDt, toDt);
            } else {
                closings = shiftClosingService.getAllClosings();
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (ShiftClosing c : closings) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", c.getId());
                item.put("staffName", c.getStaff().getFullName());
                item.put("shiftName", c.getShift().getName());
                item.put("systemRevenue", c.getSystemRevenue());
                item.put("actualCash", c.getActualCash());
                item.put("discrepancy", c.getDiscrepancy());
                item.put("note", c.getNote());
                item.put("closingTime", c.getClosingTime().toString());
                dataList.add(item);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("data", dataList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi lấy danh sách chốt ca: {}", e.getMessage(), e);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi hệ thống.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
