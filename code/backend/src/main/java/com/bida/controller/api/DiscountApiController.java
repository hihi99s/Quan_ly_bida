package com.bida.controller.api;

import com.bida.entity.DiscountCode;
import com.bida.service.DiscountCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountApiController {

    private final DiscountCodeService discountCodeService;

    @GetMapping
    public ResponseEntity<?> getAllCodes() {
        return ResponseEntity.ok(discountCodeService.getAllCodes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN tạo mã giảm giá
    public ResponseEntity<?> createCode(@RequestBody DiscountCode code) {
        try {
            DiscountCode created = discountCodeService.createCode(
                    code.getCode(),
                    code.getDiscountPercent(),
                    code.getMaxUsageCount(),
                    code.getExpiryDate()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo mã giảm giá thành công", "discount", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN sửa mã giảm giá
    public ResponseEntity<?> updateCode(@PathVariable Long id, @RequestBody DiscountCode code) {
        try {
            DiscountCode updated = discountCodeService.updateCode(
                    id,
                    code.getCode(),
                    code.getDiscountPercent(),
                    code.getMaxUsageCount(),
                    code.getExpiryDate()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công", "discount", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN bật/tắt mã
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            discountCodeService.toggleActive(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Thay đổi trạng thái mã giảm giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xóa mã giảm giá
    public ResponseEntity<?> deleteCode(@PathVariable Long id) {
        try {
            discountCodeService.deleteCode(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa mã giảm giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
