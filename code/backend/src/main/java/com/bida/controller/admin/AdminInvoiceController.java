package com.bida.controller.admin;

import com.bida.entity.Invoice;
import com.bida.entity.OrderItem;
import com.bida.entity.SessionSegment;
import com.bida.service.InvoiceService;
import com.bida.service.OrderService;
import com.bida.repository.SessionSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;
    private final SessionSegmentRepository segmentRepository;

    @GetMapping
    public String listInvoices(@RequestParam(required = false) String date,
                                Model model) {
        List<Invoice> invoices;
        if (date != null && !date.isEmpty()) {
            LocalDate d = LocalDate.parse(date);
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            invoices = invoiceService.getInvoicesByDateRange(from, to);
            model.addAttribute("filterDate", date);
        } else {
            invoices = invoiceService.getAllInvoices();
        }
        model.addAttribute("invoices", invoices);
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
