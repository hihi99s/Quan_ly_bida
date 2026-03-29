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

    /**
     * Tao hoa don tu dong khi ket thuc phien choi.
     */
    public Invoice createInvoice(Session session, User staff) {
        BigDecimal tableCharge = session.getTotalAmount();
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

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .session(session)
                .invoiceNumber(invoiceNumber)
                .tableCharge(tableCharge)
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

        log.info("Tao hoa don {} - Tien ban: {}, Dich vu: {}, Giam gia: {}, Tong: {}",
                invoiceNumber, tableCharge, serviceCharge, discount, totalAmount);

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
