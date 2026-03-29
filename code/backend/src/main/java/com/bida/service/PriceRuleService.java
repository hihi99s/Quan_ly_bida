package com.bida.service;

import com.bida.entity.PriceRule;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import com.bida.repository.PriceRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;

    /**
     * Lấy tất cả rule giá.
     */
    @Transactional(readOnly = true)
    public List<PriceRule> getAllRules() {
        return priceRuleRepository.findAll();
    }

    /**
     * Lấy rule theo loại bàn.
     */
    @Transactional(readOnly = true)
    public List<PriceRule> getRulesByTableType(TableType tableType) {
        return priceRuleRepository.findByTableType(tableType);
    }

    /**
     * Lấy rule theo loại bàn + loại ngày, sắp xếp theo giờ bắt đầu.
     */
    @Transactional(readOnly = true)
    public List<PriceRule> getRulesByTableTypeAndDay(TableType tableType, DayType dayType) {
        return priceRuleRepository.findByTableTypeAndDayTypeOrderByStartTimeAsc(tableType, dayType);
    }

    /**
     * Trả về Map<DayType, List<PriceRule>> cho 1 loại bàn,
     * mỗi list đã sắp xếp theo startTime tăng dần.
     */
    @Transactional(readOnly = true)
    public Map<DayType, List<PriceRule>> getRulesGroupedByDay(TableType tableType) {
        List<PriceRule> rules = priceRuleRepository.findByTableType(tableType);
        return rules.stream()
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.groupingBy(PriceRule::getDayType));
    }

    /**
     * Tạo mới một rule giá.
     *
     * @throws IllegalArgumentException nếu khoảng thời gian không hợp lệ
     */
    public PriceRule createRule(TableType tableType, DayType dayType,
                                LocalTime start, LocalTime end, BigDecimal price) {
        validateTimeRange(start, end);
        validatePrice(price);

        PriceRule rule = PriceRule.builder()
                .tableType(tableType)
                .dayType(dayType)
                .startTime(start)
                .endTime(end)
                .pricePerHour(price)
                .build();
        return priceRuleRepository.save(rule);
    }

    /**
     * Cập nhật rule giá theo id.
     *
     * @throws RuntimeException nếu không tìm thấy rule
     */
    public PriceRule updateRule(Long id, LocalTime start, LocalTime end, BigDecimal price) {
        validateTimeRange(start, end);
        validatePrice(price);

        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rule với id: " + id));
        rule.setStartTime(start);
        rule.setEndTime(end);
        rule.setPricePerHour(price);
        return priceRuleRepository.save(rule);
    }

    /**
     * Xóa rule giá theo id.
     */
    public void deleteRule(Long id) {
        if (!priceRuleRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy rule với id: " + id);
        }
        priceRuleRepository.deleteById(id);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá phải là số dương");
        }
    }
}
