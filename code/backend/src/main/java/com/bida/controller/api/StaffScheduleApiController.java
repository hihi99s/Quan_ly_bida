package com.bida.controller.api;

import com.bida.dto.ScheduleRequestDTO;
import com.bida.dto.ScheduleResponseDTO;
import com.bida.dto.ShiftRequestDTO;
import com.bida.dto.ShiftResponseDTO;
import com.bida.entity.ScheduleAuditLog;
import com.bida.entity.Shift;
import com.bida.entity.StaffSchedule;
import com.bida.entity.User;
import com.bida.repository.UserRepository;
import com.bida.service.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffScheduleApiController {

    private final StaffScheduleService scheduleService;
    private final UserRepository userRepository;

    // ==================== SHIFT ENDPOINTS ====================

    @GetMapping("/shifts")
    public ResponseEntity<?> getAllShifts() {
        List<ShiftResponseDTO> shifts = scheduleService.getAllShifts().stream()
                .map(this::convertShiftToDTO).toList();
        return ResponseEntity.ok(shifts);
    }

    @PostMapping("/shifts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createShift(@RequestBody ShiftRequestDTO request) {
        try {
            Shift shift = scheduleService.createShift(
                    request.getName(),
                    LocalTime.parse(request.getStartTime()),
                    LocalTime.parse(request.getEndTime()),
                    request.getMaxStaff()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Them ca thanh cong",
                    "data", convertShiftToDTO(shift)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/shifts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @RequestBody ShiftRequestDTO request) {
        try {
            Shift shift = scheduleService.updateShift(
                    id,
                    request.getName(),
                    LocalTime.parse(request.getStartTime()),
                    LocalTime.parse(request.getEndTime()),
                    request.getMaxStaff()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cap nhat ca thanh cong",
                    "data", convertShiftToDTO(shift)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/shifts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        try {
            scheduleService.deleteShift(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Da xoa ca"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== SCHEDULE ENDPOINTS ====================

    @GetMapping("/schedules")
    public ResponseEntity<?> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        List<StaffSchedule> schedules;
        if (date != null) {
            schedules = scheduleService.getSchedulesByDate(date);
        } else if (weekStart != null) {
            schedules = scheduleService.getSchedulesByWeek(weekStart);
        } else {
            LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            schedules = scheduleService.getSchedulesByWeek(monday);
        }
        return ResponseEntity.ok(schedules.stream().map(this::convertScheduleToDTO).toList());
    }

    @GetMapping("/schedules/user/{userId}")
    public ResponseEntity<?> getSchedulesByUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + userId));
            List<StaffSchedule> schedules = scheduleService.getSchedulesByUser(user, from, to);
            return ResponseEntity.ok(schedules.stream().map(this::convertScheduleToDTO).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignSchedule(@RequestBody ScheduleRequestDTO request, Principal principal) {
        try {
            StaffSchedule schedule = scheduleService.assignSchedule(
                    request.getUserId(),
                    request.getShiftId(),
                    LocalDate.parse(request.getDate()),
                    principal.getName()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xep lich thanh cong",
                    "data", convertScheduleToDTO(schedule)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body,
                                             Principal principal) {
        try {
            Long newUserId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            Long newShiftId = body.get("shiftId") != null ? Long.valueOf(body.get("shiftId").toString()) : null;

            StaffSchedule schedule = scheduleService.updateSchedule(id, newUserId, newShiftId, principal.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cap nhat lich thanh cong",
                    "data", convertScheduleToDTO(schedule)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/schedules/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id, Principal principal) {
        try {
            StaffSchedule schedule = scheduleService.checkIn(id, principal.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Check-in thanh cong",
                    "data", convertScheduleToDTO(schedule)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/schedules/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id, Principal principal) {
        try {
            StaffSchedule schedule = scheduleService.checkOut(id, principal.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Check-out thanh cong",
                    "data", convertScheduleToDTO(schedule)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id, Principal principal) {
        try {
            scheduleService.deleteSchedule(id, principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Da xoa lich"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/schedules/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkDeleteSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        try {
            int count = scheduleService.bulkDeleteSchedules(from, to, principal.getName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Da xoa " + count + " lich lam viec",
                    "deletedCount", count
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/schedules/late")
    public ResponseEntity<?> getLateCheckIns() {
        List<StaffSchedule> late = scheduleService.getLateCheckIns();
        return ResponseEntity.ok(late.stream().map(this::convertScheduleToDTO).toList());
    }

    // ==================== AUDIT LOG ENDPOINTS ====================

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAuditLogs() {
        List<ScheduleAuditLog> logs = scheduleService.getAuditLogs();
        List<Map<String, Object>> data = logs.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("scheduleId", l.getScheduleId());
            item.put("action", l.getAction());
            item.put("performedBy", l.getPerformedBy());
            item.put("performedAt", l.getPerformedAt().toString());
            item.put("details", l.getDetails());
            return item;
        }).toList();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    // ==================== DTO CONVERTERS ====================

    private ShiftResponseDTO convertShiftToDTO(Shift shift) {
        return ShiftResponseDTO.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime().toString())
                .endTime(shift.getEndTime().toString())
                .maxStaff(shift.getMaxStaff())
                .build();
    }

    private ScheduleResponseDTO convertScheduleToDTO(StaffSchedule s) {
        return ScheduleResponseDTO.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .username(s.getUser().getUsername())
                .fullName(s.getUser().getFullName())
                .shiftId(s.getShift().getId())
                .shiftName(s.getShift().getName())
                .shiftStartTime(s.getShift().getStartTime().toString())
                .shiftEndTime(s.getShift().getEndTime().toString())
                .date(s.getDate().toString())
                .status(s.getStatus().name())
                .checkInTime(s.getCheckInTime() != null ? s.getCheckInTime().toString() : null)
                .checkOutTime(s.getCheckOutTime() != null ? s.getCheckOutTime().toString() : null)
                .build();
    }
}
