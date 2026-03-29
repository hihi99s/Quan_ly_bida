package com.bida.service;

import com.bida.entity.HolidayCalendar;
import com.bida.repository.HolidayCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayService {

    private final HolidayCalendarRepository holidayRepository;

    /**
     * Kiem tra ngay co phai ngay le khong.
     */
    @Transactional(readOnly = true)
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.isHoliday(date, date.getMonthValue(), date.getDayOfMonth());
    }

    @Transactional(readOnly = true)
    public List<HolidayCalendar> getAllHolidays() {
        return holidayRepository.findAllByOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public HolidayCalendar getById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ngay le: " + id));
    }

    public HolidayCalendar createHoliday(String name, LocalDate date, boolean recurring) {
        HolidayCalendar holiday = HolidayCalendar.builder()
                .name(name)
                .date(date)
                .recurring(recurring)
                .build();
        return holidayRepository.save(holiday);
    }

    public HolidayCalendar updateHoliday(Long id, String name, LocalDate date, boolean recurring) {
        HolidayCalendar holiday = getById(id);
        holiday.setName(name);
        holiday.setDate(date);
        holiday.setRecurring(recurring);
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }
}
