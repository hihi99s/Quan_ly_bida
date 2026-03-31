package com.bida.service;

import com.bida.billing.dto.BillingResult;
import com.bida.billing.dto.BillingSegment;
import com.bida.billing.service.BillingCalculator;
import com.bida.dto.TableStatusDTO;
import com.bida.entity.*;
import com.bida.entity.enums.ReservationStatus;
import com.bida.entity.enums.SessionStatus;
import com.bida.entity.enums.TableStatus;
import com.bida.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionSegmentRepository segmentRepository;
    private final BilliardTableRepository tableRepository;
    private final UserRepository userRepository;
    private final BillingCalculator billingCalculator;
    private final InvoiceService invoiceService;
    private final OrderItemRepository orderItemRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final DiscountCodeService discountCodeService;

    // ============ HELPER METHODS ============

    /**
     * Validate table exists + handle not found.
     * Tách từ pattern lặp lại ở startSession, endSession, pauseSession, resumeSession, setMaintenance.
     */
    private BilliardTable validateAndGetTable(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ban: " + tableId));
    }

    /**
     * Calculate paused time & update session.
     * Sử dụng ở: endSession (lines 116-118), resumeSession (lines 206-208).
     * Tách để xóa duplicate logic.
     */
    private void updatePausedMinutes(Session session, LocalDateTime pauseStart) {
        if (pauseStart != null) {
            long pausedMins = ChronoUnit.MINUTES.between(pauseStart, LocalDateTime.now());
            session.setTotalPausedMinutes(session.getTotalPausedMinutes() + (int) pausedMins);
            session.setPauseStart(null);
        }
    }

    /**
     * Build single TableStatusDTO from table + optional session.
     * Tách từ getAllTableStatuses để giảm complexity (từ 65 lines → ~40 lines).
     * Xử lý cả PLAYING/PAUSED cases và RESERVED case.
     */
    private TableStatusDTO buildTableStatusDTO(BilliardTable table, Optional<Session> activeSession) {
        TableStatusDTO.TableStatusDTOBuilder dto = TableStatusDTO.builder()
                .id(table.getId())
                .name(table.getName())
                .tableType(table.getTableType().name())
                .status(table.getStatus().name())
                .currentAmount(BigDecimal.ZERO)
                .playingMinutes(0)
                .orderCount(0)
                .totalPausedMinutes(0);

        if (table.getStatus() == TableStatus.PLAYING || table.getStatus() == TableStatus.PAUSED) {
            if (activeSession.isPresent()) {
                Session session = activeSession.get();
                dto.sessionId(session.getId());
                dto.startTime(session.getStartTime());
                dto.totalPausedMinutes(session.getTotalPausedMinutes());

                // Playing minutes (tru pause)
                long totalMins = ChronoUnit.MINUTES.between(session.getStartTime(), LocalDateTime.now());
                long pausedMins = session.getTotalPausedMinutes();
                if (session.getPauseStart() != null) {
                    pausedMins += ChronoUnit.MINUTES.between(session.getPauseStart(), LocalDateTime.now());
                }
                dto.playingMinutes(Math.max(0, totalMins - pausedMins));

                // Tien tam tinh
                try {
                    dto.currentAmount(billingCalculator.calculateCurrentAmount(session));
                } catch (Exception e) {
                    dto.currentAmount(BigDecimal.ZERO);
                }

                // So luong order
                try {
                    dto.orderCount((int) orderItemRepository.countBySession(session));
                } catch (Exception e) {
                    dto.orderCount(0);
                }

                // Customer info
                if (session.getCustomer() != null) {
                    dto.customerName(session.getCustomer().getName());
                    dto.customerPhone(session.getCustomer().getPhone());
                }
            }
        } else if (table.getStatus() == TableStatus.RESERVED) {
            // Hien thi thong tin dat ban
            List<Reservation> pending = reservationRepository
                    .findByTableAndStatus(table, ReservationStatus.PENDING);
            if (!pending.isEmpty()) {
                Reservation r = pending.get(0);
                dto.reservationCustomerName(r.getCustomerName());
                dto.reservationTime(r.getReservedTime());
            }
        }

        return dto.build();
    }

    // ============ PUBLIC METHODS ============

    /**
     * Bat dau phien choi.
     */
    public Session startSession(Long tableId, String staffUsername) {
        return startSession(tableId, staffUsername, null);
    }

    /**
     * Bat dau phien choi (co the gan customer).
     */
    public Session startSession(Long tableId, String staffUsername, Long customerId) {
        BilliardTable table = validateAndGetTable(tableId);

        if (table.getStatus() == TableStatus.PLAYING || table.getStatus() == TableStatus.PAUSED) {
            throw new RuntimeException("Ban dang duoc su dung");
        }
        if (table.getStatus() == TableStatus.MAINTENANCE) {
            throw new RuntimeException("Ban dang bao tri");
        }
        if (table.getStatus() == TableStatus.DISABLED) {
            throw new RuntimeException("Ban da ngung hoat dong");
        }

        User staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + staffUsername));

        // Neu ban dang RESERVED → hoan tat reservation
        if (table.getStatus() == TableStatus.RESERVED) {
            List<Reservation> pending = reservationRepository.findByTableAndStatus(table, ReservationStatus.PENDING);
            for (Reservation r : pending) {
                r.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(r);
            }
        }

        // Doi trang thai ban
        table.setStatus(TableStatus.PLAYING);
        tableRepository.save(table);

        // Tao session moi
        Session.SessionBuilder builder = Session.builder()
                .table(table)
                .startTime(LocalDateTime.now())
                .status(SessionStatus.ACTIVE)
                .staff(staff)
                .totalAmount(BigDecimal.ZERO)
                .totalPausedMinutes(0);

        // Gan customer neu co
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer != null) {
                builder.customer(customer);
            }
        }

        Session session = builder.build();
        session = sessionRepository.save(session);
        log.info("Bat dau phien choi - Ban: {}, Staff: {}, Session: {}",
                table.getName(), staffUsername, session.getId());

        return session;
    }

    /**
     * Ket thuc phien choi va tinh tien. Tu dong tao invoice.
     *
     * FIX: Thêm error handling + fallback khi không tìm thấy PriceRule
     */
    public Session endSession(Long tableId, String staffUsername, BigDecimal manualTableCharge) {
        return endSession(tableId, staffUsername, manualTableCharge, null);
    }

    /**
     * NEW (Phase 2): Ket thuc phien choi voi ho tro discount code.
     * @param tableId - ID cua ban
     * @param staffUsername - username cua nhan vien
     * @param manualTableCharge - (tuy chon) gia ban tu cong
     * @param discountCodeStr - (tuy chon) ma giam gia
     */
    public Session endSession(Long tableId, String staffUsername, BigDecimal manualTableCharge, String discountCodeStr) {
        // Phase 2: Validate discount code EARLY — before any DB writes.
        // Prevents transaction rollback-only leak when code is invalid.
        if (discountCodeStr != null && !discountCodeStr.trim().isEmpty()) {
            discountCodeService.findAndValidate(discountCodeStr);
        }

        BilliardTable table = validateAndGetTable(tableId);

        Session session = sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Khong co phien choi nao dang hoat dong tren ban nay"));

        // Neu dang PAUSED → resume truoc khi ket thuc
        if (table.getStatus() == TableStatus.PAUSED && session.getPauseStart() != null) {
            updatePausedMinutes(session, session.getPauseStart());
        }

        session.setEndTime(LocalDateTime.now());

        // Tinh tien qua Billing Engine
        // Nếu không tìm thấy PriceRule → throw exception (đừng fallback = 0)
        // Admin phải kiểm tra bảng giá cho tableType + dayType
        BillingResult result = billingCalculator.calculate(session);
        log.info("Tinh tien thanh cong - Session #{} [{}], Ban: {}, Table Type: {}, Tong: {}",
                session.getId(), table.getTableType(), table.getName(), table.getTableType(), result.getTotalAmount());

        session.setTotalAmount(result.getTotalAmount());
        session.setStatus(SessionStatus.COMPLETED);

        // Luu cac segment chi tiet
        if (result.getSegments() != null && !result.getSegments().isEmpty()) {
            for (BillingSegment seg : result.getSegments()) {
                SessionSegment segment = SessionSegment.builder()
                        .session(session)
                        .startTime(seg.getStartTime())
                        .endTime(seg.getEndTime())
                        .durationMinutes((int) seg.getDurationMinutes())
                        .amount(seg.getAmount())
                        .build();
                segmentRepository.save(segment);
            }
        }

        sessionRepository.save(session);

        // Tra ban ve trang thai trong
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        // Tu dong tao Invoice (voi optional manual price override + discount code)
        User staff = userRepository.findByUsername(staffUsername).orElse(session.getStaff());
        try {
            invoiceService.createInvoice(session, staff, manualTableCharge, discountCodeStr);
        } catch (Exception e) {
            log.warn("Loi tao invoice cho session #{}: {}", session.getId(), e.getMessage());
        }

        log.info("Ket thuc phien choi - Ban: {}, Tong tien: {}, Session: {}",
                table.getName(), result.getTotalAmount(), session.getId());

        return session;
    }

    /**
     * Overload: backward compatibility - goi ket thuc phien ma khong co gia thu cong
     */
    public Session endSession(Long tableId, String staffUsername) {
        return endSession(tableId, staffUsername, null);
    }

    /**
     * Phase 2: Tam dung phien choi (billing dung tinh tien).
     */
    public Session pauseSession(Long tableId) {
        BilliardTable table = validateAndGetTable(tableId);

        if (table.getStatus() != TableStatus.PLAYING) {
            throw new RuntimeException("Ban khong dang choi de tam dung");
        }

        Session session = sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Khong co phien choi active"));

        session.setPauseStart(LocalDateTime.now());
        table.setStatus(TableStatus.PAUSED);

        tableRepository.save(table);
        sessionRepository.save(session);
        log.info("Tam dung phien choi - Ban: {}, Session: {}", table.getName(), session.getId());

        return session;
    }

    /**
     * Phase 2: Tiep tuc phien choi sau khi tam dung.
     */
    public Session resumeSession(Long tableId) {
        BilliardTable table = validateAndGetTable(tableId);

        if (table.getStatus() != TableStatus.PAUSED) {
            throw new RuntimeException("Ban khong dang tam dung");
        }

        Session session = sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Khong co phien choi active"));

        if (session.getPauseStart() != null) {
            updatePausedMinutes(session, session.getPauseStart());
        }

        table.setStatus(TableStatus.PLAYING);

        tableRepository.save(table);
        sessionRepository.save(session);
        log.info("Tiep tuc phien choi - Ban: {}, Tong phut tam dung: {}",
                table.getName(), session.getTotalPausedMinutes());

        return session;
    }

    /**
     * Phase 2: Bat/tat che do bao tri ban.
     */
    public void setMaintenance(Long tableId, boolean enable) {
        BilliardTable table = validateAndGetTable(tableId);

        if (enable) {
            if (table.getStatus() == TableStatus.PLAYING || table.getStatus() == TableStatus.PAUSED) {
                throw new RuntimeException("Khong the chuyen sang bao tri khi ban dang su dung");
            }
            table.setStatus(TableStatus.MAINTENANCE);
        } else {
            if (table.getStatus() != TableStatus.MAINTENANCE) {
                throw new RuntimeException("Ban khong dang o trang thai bao tri");
            }
            table.setStatus(TableStatus.AVAILABLE);
        }
        tableRepository.save(table);
        log.info("Ban {} - Bao tri: {}", table.getName(), enable);
    }

    /**
     * Tinh tien tam cho ban dang choi.
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentAmount(Long tableId) {
        BilliardTable table = tableRepository.findById(tableId).orElse(null);
        if (table == null) return BigDecimal.ZERO;

        Session session = sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElse(null);
        if (session == null) return BigDecimal.ZERO;

        try {
            return billingCalculator.calculateCurrentAmount(session);
        } catch (Exception e) {
            log.warn("Loi tinh tien tam cho ban {}: {}", tableId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Session> getActiveSession(Long tableId) {
        BilliardTable table = tableRepository.findById(tableId).orElse(null);
        if (table == null) return Optional.empty();
        return sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE);
    }

    /**
     * Build danh sach DTO cho tat ca ban.
     * Phase 2: ho tro PAUSED, RESERVED, MAINTENANCE + order count + pause info.
     */
    @Transactional(readOnly = true)
    public List<TableStatusDTO> getAllTableStatuses(List<BilliardTable> tables) {
        List<TableStatusDTO> result = new ArrayList<>();

        for (BilliardTable table : tables) {
            // Chỉ query repository nếu bàn đang chơi hoặc tạm dừng
            Optional<Session> activeSession = Optional.empty();
            if (table.getStatus() == TableStatus.PLAYING || table.getStatus() == TableStatus.PAUSED) {
                activeSession = sessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE);
            }

            result.add(buildTableStatusDTO(table, activeSession));
        }

        return result;
    }
}
