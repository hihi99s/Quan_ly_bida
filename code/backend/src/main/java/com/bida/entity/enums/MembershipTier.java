package com.bida.entity.enums;

import java.math.BigDecimal;

public enum MembershipTier {
    BRONZE(BigDecimal.ZERO),
    SILVER(new BigDecimal("5")),
    GOLD(new BigDecimal("10")),
    DIAMOND(new BigDecimal("15"));

    private final BigDecimal discountPercent;

    MembershipTier(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Xac dinh tier dua tren tong chi tieu.
     * BRONZE: 0-500k, SILVER: 500k-2M, GOLD: 2M-5M, DIAMOND: >5M
     */
    public static MembershipTier fromTotalSpent(BigDecimal totalSpent) {
        if (totalSpent.compareTo(new BigDecimal("5000000")) >= 0) return DIAMOND;
        if (totalSpent.compareTo(new BigDecimal("2000000")) >= 0) return GOLD;
        if (totalSpent.compareTo(new BigDecimal("500000")) >= 0) return SILVER;
        return BRONZE;
    }
}
