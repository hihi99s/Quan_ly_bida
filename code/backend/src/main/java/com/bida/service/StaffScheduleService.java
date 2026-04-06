package com.bida.service;

import com.bida.entity.ScheduleAuditLog;
import com.bida.entity.Shift;
import com.bida.entity.StaffSchedule;
import com.bida.entity.User;
import com.bida.entity.enums.ScheduleStatus;
import com.bida.repository.ScheduleAuditLogRepository;
import com.bida.repository.ShiftRepository;
import com.bida.repository.StaffScheduleRepository;
import com.bida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffScheduleService {

    private final StaffScheduleRepository scheduleRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ScheduleAuditLogRepository auditLogRepository;

    // ==================== Audit Helper ====================

    private void logAudit(Long scheduleId, String action, String performedBy, String details) {
        ScheduleAuditLog auditLog = ScheduleAuditLog.builder()
                .scheduleId(scheduleId)
                .action(action)
                .performedBy(performedBy != null ? performedBy : "system")
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    // ==================== Shift CRUD ====================

    @Transactional(readOnly = true)
    public List<Shift> getAllShifts() {
        return shiftRepository.findAllByOrderByStartTimeAsc();
    }

    public Shift createShift(String name, java.time.LocalTime startTime, java.time.LocalTime endTime, Integer maxStaff) {
        Shift shift = Shift.builder()
                .name(name)
                .startTime(startTime)
                .endTime(endTime)
                .maxStaff(maxStaff)
                .build();
        return shiftRepository.save(shift);
    }

    public Shift updateShift(Long id, String name, java.time.LocalTime startTime, java.time.LocalTime endTime, Integer maxStaff) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ca: " + id));
        shift.setName(name);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setMaxStaff(maxStaff);
        return shiftRepository.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    // ==================== Schedule CRUD ====================

    @Transactional(readOnly = true)
    public List<StaffSchedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByDate(date);
    }

    @Transactional(readOnly = true)
    public List<StaffSchedule> getSchedulesByWeek(LocalDate startOfWeek) {
        return scheduleRepository.findByDateBetween(startOfWeek, startOfWeek.plusDays(6));
    }

    @Transactional(readOnly = true)
    public List<StaffSchedule> getSchedulesByUser(User user, LocalDate from, LocalDate to) {
        return scheduleRepository.findByUserAndDateBetween(user, from, to);
    }

    /**
     * Phan cong nhan vien vao ca.
     * - Khoa lich qua khu (Feature 4)
     * - Gioi han maxStaff (Feature 3)
     * - Audit log (Feature 5)
     */
    public StaffSchedule assignSchedule(Long userId, Long shiftId, LocalDate date, String performedBy) {
        // Feature 4: Khoa lich qua khu
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Khong the xep lich cho ngay da qua: " + date);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + userId));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ca: " + shiftId));

        // Kiem tra trung lich / chong lan gio
        List<StaffSchedule> existingSchedules = scheduleRepository.findByUserAndDate(user, date);
        for (StaffSchedule existing : existingSchedules) {
            if (isOverlapping(shift, existing.getShift())) {
                throw new RuntimeException("Nhan vien " + user.getFullName() + 
                    " bi trung gio lam voi ca " + existing.getShift().getName() + 
                    " (" + existing.getShift().getStartTime() + " - " + existing.getShift().getEndTime() + ")");
            }
        }

        // Feature 3: maxStaff enforcement
        if (shift.getMaxStaff() != null) {
            long currentCount = scheduleRepository.countByShiftAndDate(shift, date);
            if (currentCount >= shift.getMaxStaff()) {
                throw new RuntimeException("Ca " + shift.getName() + " ngay " + date
                        + " da du " + shift.getMaxStaff() + " nhan vien");
            }
        }

        StaffSchedule schedule = StaffSchedule.builder()
                .user(user)
                .shift(shift)
                .date(date)
                .status(ScheduleStatus.SCHEDULED)
                .build();

        StaffSchedule saved = scheduleRepository.save(schedule);

        // Feature 5: Audit log
        logAudit(saved.getId(), "CREATED", performedBy,
                "Xep lich cho " + user.getFullName() + " ca " + shift.getName() + " ngay " + date);

        return saved;
    }

    /**
     * Cap nhat lich: doi nhan vien hoac doi ca.
     * - Khoa lich qua khu (Feature 4)
     * - Gioi han maxStaff (Feature 3)
     * - Audit log (Feature 5)
     */
    public StaffSchedule updateSchedule(Long scheduleId, Long newUserId, Long newShiftId, String performedBy) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + scheduleId));

        // Feature 4: Khoa lich qua khu
        if (schedule.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Khong the sua lich cua ngay da qua: " + schedule.getDate());
        }

        StringBuilder changes = new StringBuilder();

        if (newUserId != null && !newUserId.equals(schedule.getUser().getId())) {
            User newUser = userRepository.findById(newUserId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + newUserId));
            // Kiem tra trung / chong lan
            List<StaffSchedule> existingSchedules = scheduleRepository.findByUserAndDate(newUser, schedule.getDate());
            for (StaffSchedule existing : existingSchedules) {
                if (!existing.getId().equals(scheduleId) && isOverlapping(schedule.getShift(), existing.getShift())) {
                    throw new RuntimeException("Nhan vien " + newUser.getFullName()
                            + " bi trung gio voi ca " + existing.getShift().getName() 
                            + " ngay " + schedule.getDate());
                }
            }
            changes.append("Doi nhan vien: ").append(schedule.getUser().getFullName())
                    .append(" -> ").append(newUser.getFullName()).append(". ");
            schedule.setUser(newUser);
        }

        if (newShiftId != null && !newShiftId.equals(schedule.getShift().getId())) {
            Shift newShift = shiftRepository.findById(newShiftId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay ca: " + newShiftId));
            // Feature 3: maxStaff check
            if (newShift.getMaxStaff() != null) {
                long currentCount = scheduleRepository.countByShiftAndDate(newShift, schedule.getDate());
                if (currentCount >= newShift.getMaxStaff()) {
                    throw new RuntimeException("Ca " + newShift.getName() + " ngay " + schedule.getDate()
                            + " da du " + newShift.getMaxStaff() + " nhan vien");
                }
            }
            changes.append("Doi ca: ").append(schedule.getShift().getName())
                    .append(" -> ").append(newShift.getName()).append(". ");
            schedule.setShift(newShift);
        }

        StaffSchedule saved = scheduleRepository.save(schedule);

        // Feature 5: Audit
        logAudit(saved.getId(), "UPDATED", performedBy, changes.toString());

        return saved;
    }

    /**
     * Check-in ca.
     */
    public StaffSchedule checkIn(Long scheduleId, String performedBy) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + scheduleId));

        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new RuntimeException("Trang thai khong hop le de check-in");
        }

        schedule.setStatus(ScheduleStatus.CHECKED_IN);
        schedule.setCheckInTime(LocalDateTime.now());
        StaffSchedule saved = scheduleRepository.save(schedule);

        logAudit(saved.getId(), "CHECKED_IN", performedBy,
                "Check-in luc " + saved.getCheckInTime());

        return saved;
    }

    /**
     * Check-out ca.
     */
    public StaffSchedule checkOut(Long scheduleId, String performedBy) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + scheduleId));

        if (schedule.getStatus() != ScheduleStatus.CHECKED_IN) {
            throw new RuntimeException("Chua check-in, khong the check-out");
        }

        schedule.setStatus(ScheduleStatus.CHECKED_OUT);
        schedule.setCheckOutTime(LocalDateTime.now());
        StaffSchedule saved = scheduleRepository.save(schedule);

        logAudit(saved.getId(), "CHECKED_OUT", performedBy,
                "Check-out luc " + saved.getCheckOutTime());

        return saved;
    }

    /**
     * Xoa lich don le.
     * - Khoa lich qua khu (Feature 4)
     * - Audit log (Feature 5)
     */
    public void deleteSchedule(Long id, String performedBy) {
        StaffSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + id));

        // Feature 4: Khoa lich qua khu
        if (schedule.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Khong the xoa lich cua ngay da qua: " + schedule.getDate());
        }

        // Feature 5: Audit TRUOC khi xoa
        logAudit(id, "DELETED", performedBy,
                "Xoa lich cua " + schedule.getUser().getFullName()
                        + " ca " + schedule.getShift().getName() + " ngay " + schedule.getDate());

        scheduleRepository.deleteById(id);
    }

    /**
     * Xoa lich hang loat theo khoang ngay (Feature 2).
     * Tu dong bo qua ngay qua khu (Feature 4).
     */
    public int bulkDeleteSchedules(LocalDate from, LocalDate to, String performedBy) {
        // Feature 4: Chi xoa tu hom nay tro di
        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = from.isBefore(today) ? today : from;

        if (effectiveFrom.isAfter(to)) {
            throw new RuntimeException("Khong co lich nao trong tuong lai de xoa (tu " + from + " den " + to + ")");
        }

        List<StaffSchedule> toDelete = scheduleRepository.findByDateBetween(effectiveFrom, to);

        if (toDelete.isEmpty()) {
            throw new RuntimeException("Khong co lich nao trong khoang " + effectiveFrom + " den " + to);
        }

        // Feature 5: Audit tung record
        for (StaffSchedule s : toDelete) {
            logAudit(s.getId(), "DELETED", performedBy,
                    "Xoa hang loat: " + s.getUser().getFullName()
                            + " ca " + s.getShift().getName() + " ngay " + s.getDate());
        }

        int count = toDelete.size();
        scheduleRepository.deleteAll(toDelete);
        log.info("Xoa hang loat {} lich tu {} den {} boi {}", count, effectiveFrom, to, performedBy);

        return count;
    }

    /**
     * Danh sach nhan vien chua check-in nhung da den gio.
     */
    @Transactional(readOnly = true)
    public List<StaffSchedule> getLateCheckIns() {
        return scheduleRepository.findLateCheckIns(LocalDate.now());
    }

    // ==================== Audit Log Queries ====================

    @Transactional(readOnly = true)
    public List<ScheduleAuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByPerformedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ScheduleAuditLog> getAuditLogsBySchedule(Long scheduleId) {
        return auditLogRepository.findByScheduleIdOrderByPerformedAtDesc(scheduleId);
    }

    private boolean isOverlapping(Shift s1, Shift s2) {
        return s1.getStartTime().isBefore(s2.getEndTime()) && s2.getStartTime().isBefore(s1.getEndTime());
    }
}
