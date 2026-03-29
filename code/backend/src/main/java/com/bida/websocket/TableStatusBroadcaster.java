package com.bida.websocket;

import com.bida.dto.TableStatusDTO;
import com.bida.entity.BilliardTable;
import com.bida.service.SessionService;
import com.bida.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final TableService tableService;
    private final SessionService sessionService;

    /**
     * Broadcast toàn bộ trạng thái bàn tới tất cả clients.
     */
    public void broadcastAllTables() {
        try {
            List<BilliardTable> tables = tableService.getAllTables();
            List<TableStatusDTO> statuses = sessionService.getAllTableStatuses(tables);
            messagingTemplate.convertAndSend("/topic/tables", statuses);
            log.debug("Broadcast {} table statuses", statuses.size());
        } catch (Exception e) {
            log.error("Lỗi broadcast table status: {}", e.getMessage());
        }
    }
}
