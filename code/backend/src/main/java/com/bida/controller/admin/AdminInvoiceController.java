package com.bida.controller.admin;

import com.bida.entity.Invoice;
import com.bida.entity.OrderItem;
import com.bida.entity.SessionSegment;
import com.bida.service.InvoiceService;
import com.bida.service.OrderService;
import com.bida.repository.SessionSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;
    private final SessionSegmentRepository segmentRepository;

    @GetMapping
    public String listInvoices(@RequestParam(required = false) String from,
                                @RequestParam(required = false) String to,
                                Model model) {
        List<Invoice> invoices;
        String errorMsg = null;

        // FIX: Thêm validation + error handling cho date range filter
        if (from != null && !from.isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(from);
                LocalDate toDate = fromDate;

                // Parse toDate nếu có, nếu không thì mặc định = fromDate
                if (to != null && !to.isEmpty()) {
                    try {
                        toDate = LocalDate.parse(to);
                    } catch (DateTimeParseException e) {
                        log.warn("Invalid 'to' date format: {} - using fromDate instead", to);
                        toDate = fromDate;
                    }
                }

                // Validate logic: fromDate không được > toDate
                if (fromDate.isAfter(toDate)) {
                    log.warn("Invalid date range: from={} > to={}, swapping", fromDate, toDate);
                    LocalDate temp = fromDate;
                    fromDate = toDate;
                    toDate = temp;
                }

                LocalDateTime fromDT = fromDate.atStartOfDay();
                LocalDateTime toDT = toDate.plusDays(1).atStartOfDay();
                invoices = invoiceService.getInvoicesByDateRange(fromDT, toDT);

                if (invoices == null || invoices.isEmpty()) {
                    log.debug("No invoices found for date range: {} to {}", fromDate, toDate);
                    errorMsg = "Không tìm thấy hóa đơn trong khoảng thời gian này.";
                }

                model.addAttribute("from", from);
                model.addAttribute("to", to);
            } catch (DateTimeParseException e) {
                log.error("Invalid 'from' date format: {}", from, e);
                errorMsg = "Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng yyyy-MM-dd";
                invoices = invoiceService.getAllInvoices();
            } catch (Exception e) {
                log.error("Error filtering invoices by date range", e);
                errorMsg = "Lỗi khi lọc hóa đơn: " + e.getMessage();
                invoices = invoiceService.getAllInvoices();
            }
        } else {
            invoices = invoiceService.getAllInvoices();
        }

        if (invoices == null) {
            invoices = List.of();
        }

        model.addAttribute("invoices", invoices);
        if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
        }
        return "admin/invoices";
    }

    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getById(id);
        model.addAttribute("invoice", invoice);

        // Lay chi tiet segments
        List<SessionSegment> segments = segmentRepository.findBySession(invoice.getSession());
        model.addAttribute("segments", segments);

        // Lay chi tiet order items
        List<OrderItem> orderItems = orderService.getOrdersBySession(invoice.getSession().getId());
        model.addAttribute("orderItems", orderItems);

        return "admin/invoice-detail";
    }
}
