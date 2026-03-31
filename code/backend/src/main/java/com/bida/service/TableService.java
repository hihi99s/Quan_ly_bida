package com.bida.service;

import com.bida.entity.BilliardTable;
import com.bida.entity.enums.TableStatus;
import com.bida.entity.enums.TableType;
import com.bida.repository.BilliardTableRepository;
import com.bida.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TableService {

    private final BilliardTableRepository tableRepository;
    private final SessionRepository sessionRepository;

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

    public void disableTable(Long id) {
        BilliardTable table = getById(id);
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Không thể ngừng hoạt động bàn đang " +
                    getStatusLabel(table.getStatus()));
        }
        table.setStatus(TableStatus.DISABLED);
        tableRepository.save(table);
    }

    public void enableTable(Long id) {
        BilliardTable table = getById(id);
        if (table.getStatus() != TableStatus.DISABLED) {
            throw new RuntimeException("Chỉ có thể kích hoạt lại bàn đã ngừng hoạt động");
        }
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        BilliardTable table = getById(id);

        // Check if table is currently in use
        if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.DISABLED) {
            throw new RuntimeException("Không thể xóa bàn đang hoạt động");
        }

        // Check if table has any session history
        long sessionCount = sessionRepository.countByTable(table);
        if (sessionCount > 0) {
            throw new RuntimeException("Không thể xóa bàn đã có lịch sử phiên chơi");
        }

        tableRepository.delete(table);
    }

    private String getStatusLabel(TableStatus status) {
        return switch(status) {
            case PLAYING -> "chơi";
            case PAUSED -> "tạm dừng";
            case RESERVED -> "đặt trước";
            case MAINTENANCE -> "bảo trì";
            case DISABLED -> "ngừng hoạt động";
            default -> "không xác định";
        };
    }
}
