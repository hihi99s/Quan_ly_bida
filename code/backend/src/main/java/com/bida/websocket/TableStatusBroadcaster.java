package com.bida.websocket;

import com.bida.dto.TableStatusDTO;
import com.bida.entity.BilliardTable;
import com.bida.service.SessionService;
import com.bida.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service để broadcast trạng thái bàn qua WebSocket.
 *
 * FIX (Error 5): Thêm @Scheduled method để broadcast timer realtime mỗi 5 giây.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final TableService tableService;
    private final SessionService sessionService;

    /**
     * Broadcast toàn bộ trạng thái bàn tới tất cả clients.
     * Gọi trực tiếp khi có event (start/end/pause/resume session).
     */
    public void broadcastAllTables() {
        try {
            List<BilliardTable> tables = tableService.getAllTables();
            List<TableStatusDTO> statuses = sessionService.getAllTableStatuses(tables);

            long activeTables = statuses.stream()
                    .filter(s -> s.getSessionId() != null)
                    .count();

            messagingTemplate.convertAndSend("/topic/tables", statuses);
            log.info("✓ Broadcast on-demand - {} tables (active: {})", statuses.size(), activeTables);
        } catch (Exception e) {
            log.error("✗ LỖI broadcast on-demand: {}", e.getMessage(), e);
        }
    }

    /**
     * FIX: Periodic broadcast mỗi 5 giây để update timer realtime.
     *
     * Spring will automatically call this method every 5000ms.
     * Ensure @EnableScheduling is present in main application config.
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void broadcastTableStatusPeriodically() {
        try {
            List<BilliardTable> tables = tableService.getAllTables();
            List<TableStatusDTO> statuses = sessionService.getAllTableStatuses(tables);

            long activeTables = statuses.stream()
                    .filter(s -> s.getSessionId() != null)
                    .count();

            messagingTemplate.convertAndSend("/topic/tables", statuses);

            if (activeTables > 0) {
                log.info("✓ Broadcast periodic [5s] - {} tables (active: {})",
                        statuses.size(), activeTables);
            } else {
                log.debug("✓ Broadcast periodic [5s] - {} tables (all idle)",
                        statuses.size());
            }
        } catch (Exception e) {
            log.error("✗ LỖI periodic broadcast: {}", e.getMessage(), e);
        }
    }
}
