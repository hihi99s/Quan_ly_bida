package com.bida.service;

import com.bida.entity.Shift;
import com.bida.entity.StaffSchedule;
import com.bida.entity.User;
import com.bida.entity.enums.ScheduleStatus;
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

    // ---- Shift CRUD ----

    @Transactional(readOnly = true)
    public List<Shift> getAllShifts() {
        return shiftRepository.findAllByOrderByStartTimeAsc();
    }

    public Shift createShift(String name, java.time.LocalTime startTime, java.time.LocalTime endTime) {
        Shift shift = Shift.builder()
                .name(name)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return shiftRepository.save(shift);
    }

    public Shift updateShift(Long id, String name, java.time.LocalTime startTime, java.time.LocalTime endTime) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ca: " + id));
        shift.setName(name);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        return shiftRepository.save(shift);
    }

    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }

    // ---- Schedule ----

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

    public StaffSchedule assignSchedule(Long userId, Long shiftId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + userId));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ca: " + shiftId));

        // Kiem tra trung lich
        scheduleRepository.findByUserAndDate(user, date).ifPresent(existing -> {
            throw new RuntimeException("Nhan vien da co lich lam ngay " + date);
        });

        StaffSchedule schedule = StaffSchedule.builder()
                .user(user)
                .shift(shift)
                .date(date)
                .status(ScheduleStatus.SCHEDULED)
                .build();

        return scheduleRepository.save(schedule);
    }

    /**
     * Check-in ca.
     */
    public StaffSchedule checkIn(Long scheduleId) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + scheduleId));

        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new RuntimeException("Trang thai khong hop le de check-in");
        }

        schedule.setStatus(ScheduleStatus.CHECKED_IN);
        schedule.setCheckInTime(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }

    /**
     * Check-out ca.
     */
    public StaffSchedule checkOut(Long scheduleId) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lich: " + scheduleId));

        if (schedule.getStatus() != ScheduleStatus.CHECKED_IN) {
            throw new RuntimeException("Chua check-in, khong the check-out");
        }

        schedule.setStatus(ScheduleStatus.CHECKED_OUT);
        schedule.setCheckOutTime(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    /**
     * Danh sach nhan vien chua check-in nhung da den gio.
     */
    @Transactional(readOnly = true)
    public List<StaffSchedule> getLateCheckIns() {
        return scheduleRepository.findLateCheckIns(LocalDate.now());
    }
}
