package com.bida.entity;

import com.bida.entity.enums.MembershipTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipTier membershipTier = MembershipTier.BRONZE;

    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Cap nhat tong chi tieu va tu dong upgrade tier.
     */
    public void addSpending(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
        // Tich diem: 1,000 VND = 1 point
        this.points += amount.divideToIntegralValue(new BigDecimal("1000")).intValue();
        // Auto-upgrade tier
        this.membershipTier = MembershipTier.fromTotalSpent(this.totalSpent);
    }
}
