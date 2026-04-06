package com.bida.controller.api;

import com.bida.entity.Invoice;
import com.bida.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceApiController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<?> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices().stream().map(this::convertToDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInvoiceDetail(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.getById(id);
            return ResponseEntity.ok(convertToDTO(invoice));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    private com.bida.dto.InvoiceResponseDTO convertToDTO(Invoice inv) {
        return com.bida.dto.InvoiceResponseDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .tableName(inv.getSession() != null && inv.getSession().getTable() != null ? inv.getSession().getTable().getName() : "Unknown")
                .tableType(inv.getSession() != null && inv.getSession().getTable() != null ? inv.getSession().getTable().getTableType().toString() : "")
                .tableCharge(inv.getTableCharge())
                .serviceCharge(inv.getServiceCharge())
                .discount(inv.getDiscount())
                .codeDiscountAmount(inv.getCodeDiscountAmount())
                .discountCode(inv.getDiscountCode() != null ? inv.getDiscountCode().getCode() : null)
                .totalAmount(inv.getTotalAmount())
                .createdAt(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null)
                .staffName(inv.getStaff() != null ? inv.getStaff().getUsername() : "Admin")
                .build();
    }
}
