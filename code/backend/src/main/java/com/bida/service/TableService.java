package com.bida.service;

import com.bida.entity.BilliardTable;
import com.bida.entity.enums.TableStatus;
import com.bida.entity.enums.TableType;
import com.bida.repository.BilliardTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TableService {

    private final BilliardTableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<BilliardTable> getAllTables() {
        return tableRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public BilliardTable getById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn: " + id));
    }

    @Transactional(readOnly = true)
    public List<BilliardTable> getByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    public BilliardTable createTable(String name, TableType type) {
        BilliardTable table = BilliardTable.builder()
                .name(name)
                .tableType(type)
                .status(TableStatus.AVAILABLE)
                .build();
        return tableRepository.save(table);
    }

    public BilliardTable updateTable(Long id, String name, TableType type) {
        BilliardTable table = getById(id);
        table.setName(name);
        table.setTableType(type);
        return tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        BilliardTable table = getById(id);
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Không thể xóa bàn đang sử dụng");
        }
        tableRepository.delete(table);
    }
}
