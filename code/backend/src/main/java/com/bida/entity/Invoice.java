package com.bida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal tableCharge = BigDecimal.ZERO;

    @Column(precision = 12, scale = 0)
    private BigDecimal manualTableCharge;

    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id")
    private DiscountCode discountCode;

    @Column(precision = 12, scale = 0)
    private BigDecimal codeDiscountAmount;

    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
