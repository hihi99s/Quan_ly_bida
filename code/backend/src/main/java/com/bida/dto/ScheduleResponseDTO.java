package com.bida.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private Long shiftId;
    private String shiftName;
    private String shiftStartTime;
    private String shiftEndTime;
    private String date;
    private String status;
    private String checkInTime;
    private String checkOutTime;
}
