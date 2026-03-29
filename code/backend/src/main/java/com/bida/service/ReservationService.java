package com.bida.service;

import com.bida.entity.BilliardTable;
import com.bida.entity.Reservation;
import com.bida.entity.User;
import com.bida.entity.enums.ReservationStatus;
import com.bida.entity.enums.TableStatus;
import com.bida.repository.BilliardTableRepository;
import com.bida.repository.ReservationRepository;
import com.bida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BilliardTableRepository tableRepository;
    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    /**
     * Dat ban.
     */
    public Reservation createReservation(Long tableId, String customerName, String customerPhone,
                                          LocalDateTime reservedTime, Integer durationMinutes,
                                          String note, String staffUsername) {
        BilliardTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ban: " + tableId));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Ban khong trong de dat: " + table.getName());
        }

        User staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + staffUsername));

        Reservation reservation = Reservation.builder()
                .table(table)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .reservedTime(reservedTime)
                .durationMinutes(durationMinutes != null ? durationMinutes : 60)
                .note(note)
                .status(ReservationStatus.PENDING)
                .staff(staff)
                .build();

        // Chuyen trang thai ban sang RESERVED
        table.setStatus(TableStatus.RESERVED);
        tableRepository.save(table);

        reservation = reservationRepository.save(reservation);
        log.info("Dat ban {} cho {} luc {}", table.getName(), customerName, reservedTime);

        return reservation;
    }

    /**
     * Xac nhan dat ban.
     */
    public Reservation confirmReservation(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    /**
     * Huy dat ban.
     */
    public Reservation cancelReservation(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.CANCELLED);

        // Tra ban ve trang thai AVAILABLE
        BilliardTable table = reservation.getTable();
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        return reservationRepository.save(reservation);
    }

    /**
     * Hoan tat dat ban (khach da den, chuyen sang PLAYING).
     */
    public Reservation completeReservation(Long id) {
        Reservation reservation = getById(id);
        reservation.setStatus(ReservationStatus.COMPLETED);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dat ban: " + id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllByOrderByReservedTimeDesc();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByStatus(ReservationStatus.PENDING);
    }

    /**
     * Auto-cancel reservations qua thoi gian cho phep.
     * Mac dinh 15 phut, configurable qua AppSettings.
     */
    @Scheduled(fixedRate = 60000) // Chay moi phut
    public void autoCancelExpiredReservations() {
        String timeoutStr = appSettingService.getSetting("RESERVATION_TIMEOUT_MINUTES");
        int timeoutMinutes = timeoutStr != null ? Integer.parseInt(timeoutStr) : 15;

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Reservation> expired = reservationRepository
                .findByStatusAndReservedTimeBefore(ReservationStatus.PENDING, cutoff);

        for (Reservation r : expired) {
            r.setStatus(ReservationStatus.CANCELLED);
            BilliardTable table = r.getTable();
            if (table.getStatus() == TableStatus.RESERVED) {
                table.setStatus(TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
            reservationRepository.save(r);
            log.info("Auto-cancel dat ban #{} (ban {}) vi qua {} phut",
                    r.getId(), table.getName(), timeoutMinutes);
        }
    }
}
