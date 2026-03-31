package com.bida.entity;

import com.bida.entity.enums.TableStatus;
import com.bida.entity.enums.TableType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "billiard_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilliardTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableType tableType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private TableStatus status;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TableStatus.AVAILABLE;
        }
    }
}
