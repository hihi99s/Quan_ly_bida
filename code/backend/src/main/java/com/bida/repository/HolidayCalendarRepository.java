package com.bida.repository;

import com.bida.entity.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Long> {

    Optional<HolidayCalendar> findByDate(LocalDate date);

    List<HolidayCalendar> findByRecurringTrue();

    List<HolidayCalendar> findAllByOrderByDateAsc();

    /**
     * Kiem tra ngay co phai ngay le khong (exact date hoac recurring match day+month).
     */
    @Query("SELECT COUNT(h) > 0 FROM HolidayCalendar h WHERE h.date = :date " +
           "OR (h.recurring = true AND FUNCTION('MONTH', h.date) = :month AND FUNCTION('DAY', h.date) = :day)")
    boolean isHoliday(@Param("date") LocalDate date,
                      @Param("month") int month,
                      @Param("day") int day);
}
