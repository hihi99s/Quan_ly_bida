package com.bida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Discount code entity for Phase 2.
 * Supports percentage-based discounts (not fixed amount).
 * Tracks usage count and expiry date.
 */
@Entity
@Table(name = "discount_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;  // e.g., "SUMMER50", "NEW2024"

    @Column(nullable = false)
    private BigDecimal discountPercent;  // e.g., 5, 10, 25

    @Column
    private Integer maxUsageCount;  // null = unlimited

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDate expiryDate;  // null = never expires

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
