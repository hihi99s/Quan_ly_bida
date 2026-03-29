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

    List<OrderItem> findBySessionOrderByOrderedAtDesc(Session session);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM OrderItem o WHERE o.session = :session")
    BigDecimal sumAmountBySession(@Param("session") Session session);

    @Query("SELECT COUNT(o) FROM OrderItem o WHERE o.session = :session")
    long countBySession(@Param("session") Session session);
}
