package com.bida.service;

import com.bida.entity.Shift;
import com.bida.entity.ShiftClosing;
import com.bida.entity.User;
import com.bida.repository.InvoiceRepository;
import com.bida.repository.ShiftClosingRepository;
import com.bida.repository.ShiftRepository;
import com.bida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xử lý logic chốt ca.
 *
 * Quy trình:
 * 1. Nhân viên click "Chốt ca" → truyền shiftId, actualCash, note.
 * 2. Hệ thống tính systemRevenue = tổng doanh thu hóa đơn của nhân viên trong ca.
 * 3. Tính chênh lệch (discrepancy) = actualCash - systemRevenue.
 * 4. Lưu ShiftClosing → quản lý xem báo cáo chênh lệch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftClosingService {

    private final ShiftClosingRepository shiftClosingRepository;
    private final ShiftRepository shiftRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    /**
     * Chốt ca cho nhân viên hiện tại.
     *
     * @param staffUsername Username của nhân viên (lấy từ SecurityContext)
     * @param shiftId      ID ca trực
     * @param actualCash   Tiền mặt thực tế nhân viên báo cáo
     * @param note         Ghi chú giải trình (nullable)
     * @return ShiftClosing đã lưu
     */
    @Transactional
    public ShiftClosing closeShift(String staffUsername, Long shiftId,
                                    BigDecimal actualCash, String note) {

        // 1. Validate staff
        User staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + staffUsername));

        // 2. Validate shift
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca trực ID: " + shiftId));

        // 3. Tính khoảng thời gian ca trực (Xử lý thông minh cho ca đêm/ca qua ngày)
        LocalDate today = LocalDate.now();
        java.time.LocalTime nowTime = java.time.LocalTime.now();
        LocalDateTime shiftStart;
        LocalDateTime shiftEnd;

        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            // Ca đêm (ví dụ: 22:00 - 06:00)
            // Nếu đang là sáng sớm (trước 12h trưa), nhân viên đang chốt ca bắt đầu từ TỐI HÔM QUA
            if (nowTime.isBefore(java.time.LocalTime.of(12, 0))) {
                shiftStart = today.minusDays(1).atTime(shift.getStartTime());
                shiftEnd = today.atTime(shift.getEndTime());
            } else {
                // Đang là tối muộn, chốt ca bắt đầu từ TỐI NAY (ít xảy ra nhưng vẫn handle)
                shiftStart = today.atTime(shift.getStartTime());
                shiftEnd = today.plusDays(1).atTime(shift.getEndTime());
            }
        } else {
            // Ca thường trong ngày (ví dụ: 08:00 - 16:00)
            shiftStart = today.atTime(shift.getStartTime());
            shiftEnd = today.atTime(shift.getEndTime());
        }

        // 4. Kiểm tra đã chốt ca này chưa
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        boolean alreadyClosed = shiftClosingRepository.existsByStaffAndShiftAndClosingTimeBetween(
                staff, shift, dayStart, dayEnd);
        if (alreadyClosed) {
            throw new RuntimeException("Bạn đã chốt ca này rồi trong hôm nay!");
        }

        // 5. Validate tiền mặt
        if (actualCash == null || actualCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Số tiền thực tế không hợp lệ.");
        }

        // 6. Tính tổng doanh thu hệ thống ghi nhận trong ca
        BigDecimal systemRevenue = invoiceRepository.sumTotalByStaffAndDateRange(
                staff.getId(), shiftStart, shiftEnd);

        // 7. Tính chênh lệch
        BigDecimal discrepancy = actualCash.subtract(systemRevenue);

        // 8. Tạo và lưu ShiftClosing
        ShiftClosing closing = ShiftClosing.builder()
                .staff(staff)
                .shift(shift)
                .systemRevenue(systemRevenue)
                .actualCash(actualCash)
                .discrepancy(discrepancy)
                .note(note)
                .build();

        ShiftClosing saved = shiftClosingRepository.save(closing);

        log.info("Chốt ca thành công: NV={}, Ca={}, Hệ thống={}, Thực tế={}, Chênh lệch={}",
                staff.getUsername(), shift.getName(), systemRevenue, actualCash, discrepancy);

        return saved;
    }

    /**
     * Lấy danh sách tất cả các lần chốt ca (mới nhất lên đầu).
     */
    @Transactional(readOnly = true)
    public List<ShiftClosing> getAllClosings() {
        return shiftClosingRepository.findAllByOrderByClosingTimeDesc();
    }

    /**
     * Lấy danh sách chốt ca theo khoảng thời gian.
     */
    @Transactional(readOnly = true)
    public List<ShiftClosing> getClosingsByDateRange(LocalDateTime from, LocalDateTime to) {
        return shiftClosingRepository.findByClosingTimeBetweenOrderByClosingTimeDesc(from, to);
    }

    /**
     * Lấy danh sách chốt ca theo nhân viên.
     */
    @Transactional(readOnly = true)
    public List<ShiftClosing> getClosingsByStaff(Long staffId) {
        return shiftClosingRepository.findByStaffIdOrderByClosingTimeDesc(staffId);
    }
}
