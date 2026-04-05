package com.bida.controller.api;

import com.bida.entity.PriceRule;
import com.bida.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceRuleApiController {

    private final PriceRuleService priceRuleService;

    @GetMapping
    public ResponseEntity<?> getAllRules() {
        return ResponseEntity.ok(priceRuleService.getAllRules());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN tạo bảng giá
    public ResponseEntity<?> createRule(@RequestBody PriceRule rule) {
        try {
            PriceRule created = priceRuleService.createRule(
                    rule.getTableType(),
                    rule.getDayType(),
                    rule.getStartTime(),
                    rule.getEndTime(),
                    rule.getPricePerHour()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm khung giá thành công", "rule", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN sửa giá
    public ResponseEntity<?> updateRule(@PathVariable Long id, @RequestBody PriceRule rule) {
        try {
            PriceRule updated = priceRuleService.updateRule(
                    id,
                    rule.getStartTime(),
                    rule.getEndTime(),
                    rule.getPricePerHour()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật khung giá thành công", "rule", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xóa giá
    public ResponseEntity<?> deleteRule(@PathVariable Long id) {
        try {
            priceRuleService.deleteRule(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa khung giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
