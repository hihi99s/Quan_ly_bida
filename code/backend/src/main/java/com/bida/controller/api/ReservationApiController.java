package com.bida.controller.api;

import com.bida.entity.Reservation;
import com.bida.service.ReservationService;
import com.bida.websocket.TableStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;
    private final TableStatusBroadcaster broadcaster;

    /**
     * POST /api/reservations - Dat ban.
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody com.bida.dto.ReservationRequestDTO request, Principal principal) {
        try {
            // Ho tro ca YYYY-MM-DDTHH:mm va ISO8601 full
            String timeStr = request.getReservedTime();
            if (timeStr.contains("Z")) timeStr = timeStr.replace("Z", "");
            if (timeStr.contains(".")) timeStr = timeStr.substring(0, timeStr.lastIndexOf("."));

            Reservation r = reservationService.createReservation(
                    request.getTableId(), request.getCustomerName(), request.getCustomerPhone(),
                    LocalDateTime.parse(timeStr),
                    request.getDurationMinutes(), request.getNote(), principal.getName());
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Dat ban thanh cong",
                    "reservationId", r.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/reservations/{id}/cancel - Huy dat ban.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da huy dat ban"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/reservations - Danh sach dat ban.
     */
    @GetMapping
    public ResponseEntity<?> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations().stream().map(this::convertToDTO).toList());
    }

    /**
     * GET /api/reservations/pending - Dat ban dang cho.
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingReservations() {
        return ResponseEntity.ok(reservationService.getPendingReservations().stream().map(this::convertToDTO).toList());
    }

    private com.bida.dto.ReservationResponseDTO convertToDTO(Reservation r) {
        return com.bida.dto.ReservationResponseDTO.builder()
                .id(r.getId())
                .tableName(r.getTable() != null ? r.getTable().getName() : "Unknown")
                .tableType(r.getTable() != null ? r.getTable().getTableType().toString() : "")
                .customerName(r.getCustomerName())
                .customerPhone(r.getCustomerPhone())
                .reservedTime(r.getReservedTime().toString())
                .durationMinutes(r.getDurationMinutes())
                .note(r.getNote())
                .status(r.getStatus().toString())
                .staffName(r.getStaff() != null ? r.getStaff().getUsername() : "")
                .build();
    }
}
