package com.bida.controller.api;

import com.bida.dto.TableStatusDTO;
import com.bida.entity.BilliardTable;
import com.bida.entity.Invoice;
import com.bida.entity.OrderItem;
import com.bida.entity.Session;
import com.bida.entity.enums.TableType;
import com.bida.service.*;
import com.bida.websocket.TableStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableApiController {

    private final TableService tableService;
    private final SessionService sessionService;
    private final TableStatusBroadcaster broadcaster;
    private final OrderService orderService;
    private final InvoiceService invoiceService;

    /**
     * GET /api/tables - Danh sach tat ca ban + trang thai realtime (exclude DISABLED).
     */
    @GetMapping
    public ResponseEntity<List<TableStatusDTO>> getAllTables() {
        List<BilliardTable> tables = tableService.getAllTables();
        // Filter out DISABLED tables from dashboard
        tables = tables.stream()
                .filter(t -> !t.getStatus().equals(com.bida.entity.enums.TableStatus.DISABLED))
                .toList();
        List<TableStatusDTO> statuses = sessionService.getAllTableStatuses(tables);
        return ResponseEntity.ok(statuses);
    }

    /**
     * POST /api/tables/{id}/start - Bat dau phien choi.
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startSession(@PathVariable Long id,
                                           @RequestParam(required = false) Long customerId,
                                           Principal principal) {
        try {
            Session session = sessionService.startSession(id, principal.getName(), customerId);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Da bat dau phien choi",
                    "sessionId", session.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/tables/{id}/end - Ket thuc phien choi.
     * @param manualTableCharge - (tuy chon) gia ban su dung thu cong
     * @param discountCode - (tuy chon) ma giam gia
     */
    @PostMapping("/{id}/end")
    public ResponseEntity<?> endSession(@PathVariable Long id,
                                        @RequestParam(required = false) BigDecimal manualTableCharge,
                                        @RequestParam(required = false) String discountCode,
                                        Principal principal) {
        try {
            Session session = sessionService.endSession(id, principal.getName(), manualTableCharge, discountCode);
            broadcaster.broadcastAllTables();

            // Lay invoice
            Optional<Invoice> invoice = invoiceService.getBySessionId(session.getId());
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Da ket thuc phien choi");
            response.put("totalAmount", session.getTotalAmount());
            if (invoice.isPresent()) {
                Invoice inv = invoice.get();
                response.put("invoiceId", inv.getId());
                response.put("invoiceNumber", inv.getInvoiceNumber());
                response.put("startTime", session.getStartTime());
                response.put("endTime", session.getEndTime());
                response.put("tableCharge", inv.getTableCharge());
                response.put("serviceCharge", inv.getServiceCharge());
                response.put("discount", inv.getDiscount());
                response.put("codeDiscountAmount", inv.getCodeDiscountAmount());
                response.put("invoiceTotal", inv.getTotalAmount());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/tables/{id}/pause - Tam dung phien choi (Phase 2).
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pauseSession(@PathVariable Long id) {
        try {
            sessionService.pauseSession(id);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da tam dung"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/tables/{id}/resume - Tiep tuc phien choi (Phase 2).
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resumeSession(@PathVariable Long id) {
        try {
            sessionService.resumeSession(id);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da tiep tuc"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/tables/{id}/maintenance - Bat/tat bao tri (Phase 2).
     */
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<?> toggleMaintenance(@PathVariable Long id,
                                                @RequestParam boolean enable) {
        try {
            sessionService.setMaintenance(id, enable);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true,
                    "message", enable ? "Da bat bao tri" : "Da tat bao tri"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/tables/{id}/current - Tien tam tinh.
     */
    @GetMapping("/{id}/current")
    public ResponseEntity<?> getCurrentAmount(@PathVariable Long id) {
        BigDecimal amount = sessionService.getCurrentAmount(id);
        return ResponseEntity.ok(Map.of("currentAmount", amount));
    }

    /**
     * POST /api/tables/{id}/transfer?targetTableId={tid} - Chuyen ban.
     *
     * Ban nguon ({id})     : phai dang PLAYING hoac PAUSED.
     * Ban dich (targetTableId): phai dang AVAILABLE.
     * Session, customer, order items, pause info duoc giu nguyen.
     */
    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> transferSession(@PathVariable Long id,
                                              @RequestParam Long targetTableId,
                                              Principal principal) {
        try {
            sessionService.transferSession(id, targetTableId, principal.getName());
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Da chuyen phien choi sang ban moi thanh cong"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ---- Order (Goi mon) API ----

    /**
     * POST /api/tables/{tableId}/orders - Goi mon.
     */
    @PostMapping("/{tableId}/orders")
    public ResponseEntity<?> addOrder(@PathVariable Long tableId,
                                       @RequestParam Long productId,
                                       @RequestParam(defaultValue = "1") int quantity,
                                       Principal principal) {
        try {
            // Tim session active cua ban
            Session session = sessionService.getActiveSession(tableId)
                    .orElseThrow(() -> new RuntimeException("Ban khong co phien choi active"));
            OrderItem item = orderService.addOrder(session.getId(), productId, quantity, principal.getName());
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Da goi mon thanh cong",
                    "orderItemId", item.getId(),
                    "amount", item.getAmount()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /api/tables/{tableId}/orders - Danh sach mon da goi.
     */
    @GetMapping("/{tableId}/orders")
    public ResponseEntity<?> getOrders(@PathVariable Long tableId) {
        try {
            Session session = sessionService.getActiveSession(tableId)
                    .orElseThrow(() -> new RuntimeException("Ban khong co phien choi active"));
            List<OrderItem> items = orderService.getOrdersBySession(session.getId());
            List<Map<String, Object>> result = items.stream().map(item -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", item.getId());
                m.put("productName", item.getProduct().getName());
                m.put("quantity", item.getQuantity());
                m.put("unitPrice", item.getUnitPrice());
                m.put("amount", item.getAmount());
                m.put("orderedAt", item.getOrderedAt().toString());
                return m;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/tables/orders/{orderItemId} - Xoa mon.
     */
    @DeleteMapping("/orders/{orderItemId}")
    public ResponseEntity<?> removeOrder(@PathVariable Long orderItemId) {
        try {
            orderService.removeOrder(orderItemId);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da xoa mon"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---- Admin Table Management (CRUD) ----

    /**
     * POST /api/tables - Tao ban moi (Admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTable(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            TableType type = TableType.valueOf((String) body.get("tableType"));
            BilliardTable table = tableService.createTable(name, type);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da tao ban moi", "table", table));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/tables/{id} - Cap nhat thong tin ban (Admin only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTable(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            TableType type = TableType.valueOf((String) body.get("tableType"));
            BilliardTable table = tableService.updateTable(id, name, type);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da cap nhat ban", "table", table));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/tables/{id} - Xoa ban (Admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        try {
            tableService.deleteTable(id);
            broadcaster.broadcastAllTables();
            return ResponseEntity.ok(Map.of("success", true, "message", "Da xoa ban thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
