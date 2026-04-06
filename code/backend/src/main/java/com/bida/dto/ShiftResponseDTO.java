package com.bida.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftResponseDTO {
    private Long id;
    private String name;
    private String startTime;
    private String endTime;
    private Integer maxStaff;
}
