package com.bida.entity;

import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(
    name = "price_rules",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tableType", "dayType", "startTime"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableType tableType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayType dayType;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal pricePerHour;
}
