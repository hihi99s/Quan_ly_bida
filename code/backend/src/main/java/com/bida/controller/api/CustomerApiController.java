package com.bida.controller.api;

import com.bida.entity.Customer;
import com.bida.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerApiController {

    private final CustomerService customerService;

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
}
