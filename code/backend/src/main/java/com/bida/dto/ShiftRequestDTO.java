package com.bida.dto;

import lombok.Data;

@Data
public class ShiftRequestDTO {
    private String name;
    private String startTime;   // "HH:mm"
    private String endTime;     // "HH:mm"
    private Integer maxStaff;   // nullable - null = khong gioi han
}
