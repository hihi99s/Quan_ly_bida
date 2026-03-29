package com.bida.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableStatusDTO {

    private Long id;
    private String name;
    private String tableType;       // "POOL", "CAROM", "VIP"
    private String status;          // "AVAILABLE", "PLAYING", "PAUSED", "RESERVED", "MAINTENANCE"
    private LocalDateTime startTime;      // null neu AVAILABLE
    private BigDecimal currentAmount;     // 0 neu AVAILABLE
    private Long sessionId;               // null neu AVAILABLE
    private long playingMinutes;          // 0 neu AVAILABLE

    // ---- Phase 2 fields ----
    private int orderCount;               // So mon da goi
    private int totalPausedMinutes;       // Tong phut tam dung
    private String customerName;          // Ten khach dang choi (nullable)
    private String customerPhone;         // SDT khach (nullable)
    private String reservationCustomerName; // Ten khach dat ban (RESERVED)
    private LocalDateTime reservationTime;  // Thoi gian dat (RESERVED)
}
