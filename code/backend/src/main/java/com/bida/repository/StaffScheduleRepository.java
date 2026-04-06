package com.bida.repository;

import com.bida.entity.Shift;
import com.bida.entity.StaffSchedule;
import com.bida.entity.User;
import com.bida.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, Long> {

    List<StaffSchedule> findByDate(LocalDate date);

    List<StaffSchedule> findByUser(User user);

    List<StaffSchedule> findByDateBetween(LocalDate from, LocalDate to);

    List<StaffSchedule> findByUserAndDateBetween(User user, LocalDate from, LocalDate to);

    List<StaffSchedule> findByUserAndDate(User user, LocalDate date);

    List<StaffSchedule> findByDateAndStatus(LocalDate date, ScheduleStatus status);

    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.date = :date AND ss.status = 'SCHEDULED' " +
           "AND ss.shift.startTime <= CURRENT_TIME")
    List<StaffSchedule> findLateCheckIns(@Param("date") LocalDate date);

    long countByShiftAndDate(Shift shift, LocalDate date);
}
