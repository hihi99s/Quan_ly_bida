package com.bida.service;

import com.bida.entity.DiscountCode;
import com.bida.repository.DiscountCodeRepository;
import com.bida.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for discount code management (Phase 2).
 * Handles CRUD + validation for discount codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Find and validate a discount code.
     * Checks: active status, not expired, usage count not exceeded.
     *
     * @param code the code string
     * @return DiscountCode if valid
     * @throws IllegalArgumentException if code invalid/expired/used up
     */
    public DiscountCode findAndValidate(String code) throws IllegalArgumentException {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không được trống");
        }

        DiscountCode discountCode = discountCodeRepository.findByCode(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại: " + code));

        // Check if active
        if (!discountCode.getActive()) {
            throw new IllegalArgumentException("Mã giảm giá không còn hoạt động");
        }

        // Check if expired
        if (discountCode.getExpiryDate() != null && discountCode.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
        }

        // Check if usage limit exceeded
        if (discountCode.getMaxUsageCount() != null && discountCode.getUsageCount() >= discountCode.getMaxUsageCount()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
        }

        log.info("✓ Validated discount code: {} | Discount: {}%", code, discountCode.getDiscountPercent());
        return discountCode;
    }

    /**
     * Increment usage count after successful invoice creation.
     * Must be called within the same transaction as invoice creation.
     */
    public void incrementUsage(DiscountCode discountCode) {
        discountCode.setUsageCount(discountCode.getUsageCount() + 1);
        discountCodeRepository.save(discountCode);
        log.debug("Incremented usage for code: {} | New count: {}", discountCode.getCode(), discountCode.getUsageCount());
    }

    // ---- CRUD ----

    @Transactional(readOnly = true)
    public List<DiscountCode> getAllCodes() {
        return discountCodeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<DiscountCode> getCodeById(Long id) {
        return discountCodeRepository.findById(id);
    }

    public DiscountCode createCode(String code, java.math.BigDecimal discountPercent, Integer maxUsageCount, LocalDate expiryDate) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã không được trống");
        }
        if (discountPercent == null || discountPercent.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải > 0");
        }
        if (discountCodeRepository.findByCode(code.trim()).isPresent()) {
            throw new IllegalArgumentException("Mã đã tồn tại: " + code);
        }

        DiscountCode discountCode = DiscountCode.builder()
                .code(code.trim())
                .discountPercent(discountPercent)
                .maxUsageCount(maxUsageCount)
                .usageCount(0)
                .active(true)
                .expiryDate(expiryDate)
                .build();

        discountCodeRepository.save(discountCode);
        log.info("Created discount code: {} | Discount: {}%", code, discountPercent);
        return discountCode;
    }

    public DiscountCode updateCode(Long id, String code, java.math.BigDecimal discountPercent, Integer maxUsageCount, LocalDate expiryDate) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mã không tồn tại"));

        if (code != null && !code.trim().isEmpty() && !code.equals(discountCode.getCode())) {
            if (discountCodeRepository.findByCode(code.trim()).isPresent()) {
                throw new IllegalArgumentException("Mã đã tồn tại: " + code);
            }
            discountCode.setCode(code.trim());
        }
        if (discountPercent != null && discountPercent.compareTo(java.math.BigDecimal.ZERO) > 0) {
            discountCode.setDiscountPercent(discountPercent);
        }
        if (maxUsageCount != null) {
            discountCode.setMaxUsageCount(maxUsageCount);
        }
        discountCode.setExpiryDate(expiryDate);

        discountCodeRepository.save(discountCode);
        log.info("Updated discount code: {}", code);
        return discountCode;
    }

    public void toggleActive(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mã không tồn tại"));
        discountCode.setActive(!discountCode.getActive());
        discountCodeRepository.save(discountCode);
        log.info("Toggled active status for code: {} | New status: {}", discountCode.getCode(), discountCode.getActive());
    }

    public void deleteCode(Long id) {
        discountCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mã không tồn tại"));

        if (invoiceRepository.existsByDiscountCodeId(id)) {
            throw new IllegalArgumentException(
                "Không thể xóa mã giảm giá đã được sử dụng trong hóa đơn. " +
                "Vui lòng vô hiệu hóa mã thay vì xóa.");
        }

        discountCodeRepository.deleteById(id);
        log.info("Deleted discount code id: {}", id);
    }
}
