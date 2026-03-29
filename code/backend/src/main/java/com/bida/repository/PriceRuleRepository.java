package com.bida.repository;

import com.bida.entity.PriceRule;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    List<PriceRule> findByTableTypeAndDayType(TableType tableType, DayType dayType);

    List<PriceRule> findByTableTypeAndDayTypeOrderByStartTimeAsc(TableType tableType, DayType dayType);

    List<PriceRule> findByTableType(TableType tableType);

    boolean existsByTableTypeAndDayTypeAndStartTime(TableType tableType, DayType dayType, LocalTime startTime);
}
