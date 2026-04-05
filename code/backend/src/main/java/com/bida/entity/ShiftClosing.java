package com.bida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ thông tin chốt ca.
 * Mỗi record đại diện cho một lần chốt ca của nhân viên,
 * ghi nhận doanh thu hệ thống, tiền mặt thực tế, và chênh lệch.
 */
@Entity
@Table(name = "shift_closings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nhân viên chốt ca */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    /** Ca trực */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /** Tổng doanh thu ghi nhận bởi hệ thống (từ Invoice) */
    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal systemRevenue = BigDecimal.ZERO;

    /** Số tiền mặt thực tế nhân viên nhập */
    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal actualCash = BigDecimal.ZERO;

    /** Chênh lệch = actualCash - systemRevenue (dương = thừa, âm = thiếu) */
    @Column(precision = 12, scale = 0, nullable = false)
    @Builder.Default
    private BigDecimal discrepancy = BigDecimal.ZERO;

    /** Ghi chú (giải trình chênh lệch) */
    @Column(length = 500)
    private String note;

    /** Thời gian chốt ca */
    @Column(nullable = false)
    private LocalDateTime closingTime;

    @PrePersist
    public void prePersist() {
        if (closingTime == null) {
            closingTime = LocalDateTime.now();
        }
    }
}
