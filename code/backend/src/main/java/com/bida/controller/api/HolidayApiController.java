package com.bida.controller.api;

import com.bida.entity.HolidayCalendar;
import com.bida.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayApiController {

    private final HolidayService holidayService;

    @GetMapping
    public ResponseEntity<?> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN thêm ngày lễ
    public ResponseEntity<?> createHoliday(@RequestBody HolidayCalendar holiday) {
        try {
            HolidayCalendar created = holidayService.createHoliday(
                    holiday.getName(),
                    holiday.getDate(),
                    holiday.getRecurring() != null ? holiday.getRecurring() : false
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm ngày lễ thành công", "holiday", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN sửa ngày lễ
    public ResponseEntity<?> updateHoliday(@PathVariable Long id, @RequestBody HolidayCalendar holiday) {
        try {
            HolidayCalendar updated = holidayService.updateHoliday(
                    id,
                    holiday.getName(),
                    holiday.getDate(),
                    holiday.getRecurring() != null ? holiday.getRecurring() : false
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công", "holiday", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xóa ngày lễ
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
        try {
            holidayService.deleteHoliday(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa ngày lễ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
