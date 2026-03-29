package com.bida.repository;

import com.bida.entity.BilliardTable;
import com.bida.entity.Reservation;
import com.bida.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByTable(BilliardTable table);

    List<Reservation> findByTableAndStatus(BilliardTable table, ReservationStatus status);

    List<Reservation> findByReservedTimeBetweenAndStatus(
            LocalDateTime from, LocalDateTime to, ReservationStatus status);

    /**
     * Tim cac dat ban PENDING da qua thoi gian cho phep (auto-cancel).
     */
    List<Reservation> findByStatusAndReservedTimeBefore(
            ReservationStatus status, LocalDateTime cutoffTime);

    List<Reservation> findAllByOrderByReservedTimeDesc();
}
