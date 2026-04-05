package com.bida.repository;

import com.bida.entity.Shift;
import com.bida.entity.ShiftClosing;
import com.bida.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftClosingRepository extends JpaRepository<ShiftClosing, Long> {

    /** Lấy danh sách chốt ca theo nhân viên */
    List<ShiftClosing> findByStaffIdOrderByClosingTimeDesc(Long staffId);

    /** Lấy danh sách chốt ca theo khoảng thời gian */
    List<ShiftClosing> findByClosingTimeBetweenOrderByClosingTimeDesc(LocalDateTime from, LocalDateTime to);

    /** Lấy tất cả chốt ca, mới nhất lên đầu */
    List<ShiftClosing> findAllByOrderByClosingTimeDesc();

    /** Kiểm tra nhân viên đã chốt ca này trong ngày chưa (tránh duplicate) */
    boolean existsByStaffAndShiftAndClosingTimeBetween(User staff, Shift shift,
                                                       LocalDateTime from, LocalDateTime to);
}
