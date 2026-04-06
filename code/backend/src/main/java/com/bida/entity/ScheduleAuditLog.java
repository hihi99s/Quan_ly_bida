package com.bida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false, length = 20)
    private String action;  // CREATED, UPDATED, DELETED, CHECKED_IN, CHECKED_OUT

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @Column(length = 1000)
    private String details;

    @PrePersist
    public void prePersist() {
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }
}
