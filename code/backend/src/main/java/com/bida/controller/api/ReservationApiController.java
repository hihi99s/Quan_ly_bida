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
    public ResponseEntity<?> createReservation(
            @RequestParam Long tableId,
            @RequestParam String customerName,
            @RequestParam(required = false) String customerPhone,
            @RequestParam String reservedTime,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) String note,
            Principal principal) {
        try {
            Reservation r = reservationService.createReservation(
                    tableId, customerName, customerPhone,
                    LocalDateTime.parse(reservedTime),
                    durationMinutes, note, principal.getName());
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Dat ban thanh cong",
                    "reservationId", r.getId()
            ));
        } catch (RuntimeException e) {
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
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    /**
     * GET /api/reservations/pending - Dat ban dang cho.
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingReservations() {
        return ResponseEntity.ok(reservationService.getPendingReservations());
    }
}
