package com.bida.controller;

import com.bida.entity.Invoice;
import com.bida.entity.OrderItem;
import com.bida.service.InvoiceService;
import com.bida.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
public class InvoicePrintController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;

    /**
     * GET /admin/invoices/{id}/print - View printable version of invoice (Thermal 80mm).
     */
    @GetMapping("/{id}/print")
    @Transactional(readOnly = true)
    public String printInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getById(id);
        List<OrderItem> items = orderService.getOrdersBySession(invoice.getSession().getId());
        
        model.addAttribute("invoice", invoice);
        model.addAttribute("items", items);
        model.addAttribute("session", invoice.getSession());
        model.addAttribute("table", invoice.getSession().getTable());
        model.addAttribute("customer", invoice.getSession().getCustomer());
        
        return "admin/invoice-print";
    }
}
