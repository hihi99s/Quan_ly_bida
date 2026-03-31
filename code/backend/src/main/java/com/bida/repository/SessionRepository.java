package com.bida.repository;

import com.bida.entity.BilliardTable;
import com.bida.entity.Session;
import com.bida.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTableAndStatus(BilliardTable table, SessionStatus status);

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByTableOrderByStartTimeDesc(BilliardTable table);

    // ---- Phase 2: Report queries ----

    List<Session> findByStatusAndStartTimeBetween(
            SessionStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.endTime >= :from AND s.endTime < :to")
    BigDecimal sumRevenueByDateRange(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    @Query("SELECT s.table.id, COUNT(s), COALESCE(SUM(s.totalAmount), 0) FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.endTime >= :from AND s.endTime < :to " +
           "GROUP BY s.table.id ORDER BY COUNT(s) DESC")
    List<Object[]> findTableUsageStats(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    @Query("SELECT s.staff.id, s.staff.fullName, COUNT(s), COALESCE(SUM(s.totalAmount), 0) FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.endTime >= :from AND s.endTime < :to " +
           "GROUP BY s.staff.id, s.staff.fullName ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> findStaffPerformanceStats(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('HOUR', s.startTime), COUNT(s) FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.startTime >= :from AND s.startTime < :to " +
           "GROUP BY FUNCTION('HOUR', s.startTime) ORDER BY FUNCTION('HOUR', s.startTime)")
    List<Object[]> findHourlyDistribution(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('DAYOFWEEK', s.startTime), FUNCTION('HOUR', s.startTime), COUNT(s) " +
           "FROM Session s WHERE s.status = 'COMPLETED' AND s.startTime >= :from AND s.startTime < :to " +
           "GROUP BY FUNCTION('DAYOFWEEK', s.startTime), FUNCTION('HOUR', s.startTime)")
    List<Object[]> findHeatmapData(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    @Query("SELECT s.table.tableType, COALESCE(SUM(s.totalAmount), 0) FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.endTime >= :from AND s.endTime < :to " +
           "GROUP BY s.table.tableType")
    List<Object[]> findRevenueByTableType(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    List<Session> findByCustomerIdOrderByStartTimeDesc(Long customerId);

    long countByTable(BilliardTable table);
}
