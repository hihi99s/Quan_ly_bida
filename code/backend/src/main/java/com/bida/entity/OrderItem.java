package com.bida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 0, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    @PrePersist
    public void prePersist() {
        if (orderedAt == null) {
            orderedAt = LocalDateTime.now();
        }
        if (amount == null && unitPrice != null && quantity != null) {
            amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
