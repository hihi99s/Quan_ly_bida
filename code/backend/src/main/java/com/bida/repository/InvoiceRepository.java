package com.bida.repository;

import com.bida.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findBySessionId(Long sessionId);

    List<Invoice> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT i FROM Invoice i JOIN i.session s WHERE s.table.id = :tableId ORDER BY i.createdAt DESC")
    List<Invoice> findByTableId(@Param("tableId") Long tableId);

    @Query("SELECT i FROM Invoice i WHERE i.staff.id = :staffId ORDER BY i.createdAt DESC")
    List<Invoice> findByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.createdAt >= :from AND i.createdAt < :to")
    long countByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Invoice> findAllByOrderByCreatedAtDesc();

    boolean existsByDiscountCodeId(Long discountCodeId);

    /** Tổng doanh thu theo nhân viên trong khoảng thời gian (dùng cho chốt ca) */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.staff.id = :staffId AND i.createdAt >= :from AND i.createdAt < :to")
    BigDecimal sumTotalByStaffAndDateRange(@Param("staffId") Long staffId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}
