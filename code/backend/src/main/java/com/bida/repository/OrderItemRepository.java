package com.bida.repository;

import com.bida.entity.OrderItem;
import com.bida.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findBySession(Session session);

    @Query("SELECT o FROM OrderItem o JOIN FETCH o.product WHERE o.session = :session ORDER BY o.orderedAt DESC")
    List<OrderItem> findBySessionOrderByOrderedAtDesc(@Param("session") Session session);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM OrderItem o WHERE o.session = :session")
    BigDecimal sumAmountBySession(@Param("session") Session session);

    @Query("SELECT COUNT(o) FROM OrderItem o WHERE o.session = :session")
    long countBySession(@Param("session") Session session);
    @Query("SELECT o.product.name, SUM(o.quantity), SUM(o.amount) FROM OrderItem o " +
           "WHERE o.orderedAt BETWEEN :from AND :to " +
           "GROUP BY o.product.name ORDER BY SUM(o.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
