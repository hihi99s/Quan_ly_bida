package com.bida.dto;

import lombok.Data;

@Data
public class ReservationRequestDTO {
    private Long tableId;
    private String customerName;
    private String customerPhone;
    private String reservedTime;
    private Integer durationMinutes;
    private String note;
}
