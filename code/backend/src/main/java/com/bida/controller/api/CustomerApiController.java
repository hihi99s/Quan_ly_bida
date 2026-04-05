package com.bida.controller.api;

import com.bida.entity.Customer;
import com.bida.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerApiController {

    private final CustomerService customerService;

    /**
     * GET /api/customers - Lấy tất cả khách hàng
     */
    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * GET /api/customers/search?q=keyword - Tim khach hang.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam String q) {
        List<Customer> customers = customerService.search(q);
        return ResponseEntity.ok(customers.stream().map(c -> Map.of(
                "id", c.getId(),
                "name", c.getName(),
                "phone", c.getPhone(),
                "membershipTier", c.getMembershipTier().name(),
                "totalSpent", c.getTotalSpent(),
                "points", c.getPoints()
        )).toList());
    }

    /**
     * POST /api/customers - Thêm mới.
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        try {
            Customer created = customerService.createCustomer(
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm khách hàng thành công", "customer", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/customers/{id} - Cập nhật.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            Customer updated = customerService.updateCustomer(
                    id,
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công", "customer", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/customers/{id} - Xóa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN mới được xóa khách hàng
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa khách hàng thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
