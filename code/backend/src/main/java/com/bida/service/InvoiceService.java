package com.bida.service;

import com.bida.entity.*;
import com.bida.entity.enums.MembershipTier;
import com.bida.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderItemRepository orderItemRepository;
    private final SessionRepository sessionRepository;
    private final CustomerRepository customerRepository;
    private final DiscountCodeService discountCodeService;

    /**
     * Tao hoa don tu dong khi ket thuc phien choi.
     * @param session - phien choi da ket thuc
     * @param staff - nhan vien phu trach
     * @param manualTableCharge - (tuy chon) gia ban su dung thu cong, neu null thi dung gia tu dong tinh
     * @throws IllegalArgumentException neu manualTableCharge < 0
     */
    public Invoice createInvoice(Session session, User staff, BigDecimal manualTableCharge) {
        // Validate manual table charge
        if (manualTableCharge != null && manualTableCharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gia ban khong duoc am. Hay nhap gia >= 0 VND");
        }

        // Su dung gia thu cong neu co, neu khong thi dung gia tu dong
        BigDecimal tableCharge = (manualTableCharge != null)
            ? manualTableCharge
            : session.getTotalAmount();
        BigDecimal serviceCharge = orderItemRepository.sumAmountBySession(session);

        // Tinh giam gia membership
        BigDecimal discount = BigDecimal.ZERO;
        if (session.getCustomer() != null) {
            MembershipTier tier = session.getCustomer().getMembershipTier();
            BigDecimal subtotal = tableCharge.add(serviceCharge);
            discount = subtotal.multiply(tier.getDiscountPercent())
                    .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
        }

        BigDecimal totalAmount = tableCharge.add(serviceCharge).subtract(discount);

        // Duong an toan: chi cong < 0
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .session(session)
                .invoiceNumber(invoiceNumber)
                .tableCharge(tableCharge)
                .manualTableCharge(manualTableCharge)
                .serviceCharge(serviceCharge)
                .discount(discount)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .staff(staff)
                .build();

        invoice = invoiceRepository.save(invoice);

        // Cap nhat tong chi tieu cho customer (neu co)
        if (session.getCustomer() != null) {
            session.getCustomer().addSpending(totalAmount);
            customerRepository.save(session.getCustomer());
        }

        String priceSource = (manualTableCharge != null) ? "THU CONG" : "AUTO";
        log.info("Tao hoa don {} [{}] - Tien ban: {}, Dich vu: {}, Giam gia: {}, Tong: {}",
                invoiceNumber, priceSource, tableCharge, serviceCharge, discount, totalAmount);

        return invoice;
    }

    /**
     * Overload: Backward compatibility - goi tao hoa don ma khong co gia thu cong
     */
    public Invoice createInvoice(Session session, User staff) {
        return createInvoice(session, staff, null);
    }

    /**
     * NEW (Phase 2): Tao hoa don voi ho tro discount code.
     * @param session - phien choi da ket thuc
     * @param staff - nhan vien phu trach
     * @param manualTableCharge - (tuy chon) gia ban su dung thu cong
     * @param discountCodeStr - (tuy chon) ma giam gia
     * @throws IllegalArgumentException neu discount code khong hop le
     */
    public Invoice createInvoice(Session session, User staff, BigDecimal manualTableCharge, String discountCodeStr) {
        // Validate manual table charge
        if (manualTableCharge != null && manualTableCharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gia ban khong duoc am. Hay nhap gia >= 0 VND");
        }

        // Su dung gia thu cong neu co, neu khong thi dung gia tu dong
        BigDecimal tableCharge = (manualTableCharge != null)
            ? manualTableCharge
            : session.getTotalAmount();
        BigDecimal serviceCharge = orderItemRepository.sumAmountBySession(session);
        BigDecimal subtotal = tableCharge.add(serviceCharge);

        // Tinh giam gia membership (keep existing field name)
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        if (session.getCustomer() != null) {
            MembershipTier tier = session.getCustomer().getMembershipTier();
            membershipDiscount = subtotal.multiply(tier.getDiscountPercent())
                    .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
        }

        // Phase 2: Tinh giam gia discount code (NEW)
        BigDecimal codeDiscount = BigDecimal.ZERO;
        DiscountCode appliedDiscountCode = null;
        if (discountCodeStr != null && !discountCodeStr.trim().isEmpty()) {
            appliedDiscountCode = discountCodeService.findAndValidate(discountCodeStr);
            codeDiscount = subtotal.multiply(appliedDiscountCode.getDiscountPercent())
                    .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
        }

        // Tong giam gia (membership + code)
        BigDecimal totalDiscount = membershipDiscount.add(codeDiscount);

        BigDecimal totalAmount = subtotal.subtract(totalDiscount);

        // Duong an toan: chi cong < 0
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .session(session)
                .invoiceNumber(invoiceNumber)
                .tableCharge(tableCharge)
                .manualTableCharge(manualTableCharge)
                .serviceCharge(serviceCharge)
                .discount(membershipDiscount)
                .discountCode(appliedDiscountCode)
                .codeDiscountAmount(codeDiscount)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .staff(staff)
                .build();

        invoice = invoiceRepository.save(invoice);

        // Increment usage count (atomic within same transaction)
        if (appliedDiscountCode != null) {
            discountCodeService.incrementUsage(appliedDiscountCode);
        }

        // Cap nhat tong chi tieu cho customer (neu co)
        if (session.getCustomer() != null) {
            session.getCustomer().addSpending(totalAmount);
            customerRepository.save(session.getCustomer());
        }

        String priceSource = (manualTableCharge != null) ? "THU CONG" : "AUTO";
        String codeInfo = (appliedDiscountCode != null) ? (" + " + appliedDiscountCode.getCode() + "(" + appliedDiscountCode.getDiscountPercent() + "%)") : "";
        log.info("Tao hoa don {} [{}] - Tien ban: {}, Dich vu: {}, Giam gia membership: {}, Giam gia code: {}, Tong: {}{}",
                invoiceNumber, priceSource, tableCharge, serviceCharge, membershipDiscount, codeDiscount, totalAmount, codeInfo);

        return invoice;
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> getBySessionId(Long sessionId) {
        return invoiceRepository.findBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoa don: " + id));
    }

    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByDateRange(LocalDateTime from, LocalDateTime to) {
        return invoiceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByTable(Long tableId) {
        return invoiceRepository.findByTableId(tableId);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByStaff(Long staffId) {
        return invoiceRepository.findByStaffId(staffId);
    }

    /**
     * Sinh ma hoa don: INV-yyyyMMdd-xxx
     */
    private String generateInvoiceNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long count = invoiceRepository.countByDateRange(startOfDay, endOfDay);
        return String.format("INV-%s-%03d", dateStr, count + 1);
    }
}
