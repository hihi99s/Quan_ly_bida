package com.bida.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationResponseDTO {
    private Long id;
    private String tableName;
    private String tableType;
    private String customerName;
    private String customerPhone;
    private String reservedTime;
    private Integer durationMinutes;
    private String note;
    private String status;
    private String staffName;
}
