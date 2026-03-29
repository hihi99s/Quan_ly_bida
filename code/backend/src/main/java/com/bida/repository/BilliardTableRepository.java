package com.bida.repository;

import com.bida.entity.BilliardTable;
import com.bida.entity.enums.TableStatus;
import com.bida.entity.enums.TableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BilliardTableRepository extends JpaRepository<BilliardTable, Long> {

    List<BilliardTable> findByStatus(TableStatus status);

    List<BilliardTable> findByTableType(TableType tableType);

    List<BilliardTable> findAllByOrderByNameAsc();
}
