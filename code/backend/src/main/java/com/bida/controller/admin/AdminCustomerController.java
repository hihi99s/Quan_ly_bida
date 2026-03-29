package com.bida.controller.admin;

import com.bida.entity.Customer;
import com.bida.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerService.search(search);
            model.addAttribute("search", search);
        } else {
            customers = customerService.getAllCustomers();
        }
        model.addAttribute("customers", customers);
        model.addAttribute("topVIP", customerService.getTopVIP());
        return "admin/customers";
    }

    @PostMapping
    public String createCustomer(@RequestParam String name,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String email,
                                  RedirectAttributes redirectAttributes) {
        try {
            customerService.createCustomer(name, phone, email);
            redirectAttributes.addFlashAttribute("success", "Them khach hang thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/edit")
    public String updateCustomer(@PathVariable Long id,
                                  @RequestParam String name,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String email,
                                  RedirectAttributes redirectAttributes) {
        try {
            customerService.updateCustomer(id, name, phone, email);
            redirectAttributes.addFlashAttribute("success", "Cap nhat khach hang thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Da xoa khach hang");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/customers";
    }
}
