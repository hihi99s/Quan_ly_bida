package com.bida.dto;

import lombok.Data;

@Data
public class ScheduleRequestDTO {
    private Long userId;
    private Long shiftId;
    private String date;  // "yyyy-MM-dd"
}
