package com.bida.controller.admin;

import com.bida.entity.Product;
import com.bida.entity.enums.ProductCategory;
import com.bida.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("activeProducts", productService.getActiveProducts());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        model.addAttribute("categories", ProductCategory.values());
        return "admin/products";
    }

    @PostMapping
    public String createProduct(@RequestParam String name,
                                 @RequestParam ProductCategory category,
                                 @RequestParam BigDecimal price,
                                 @RequestParam(defaultValue = "0") int stockQuantity,
                                 @RequestParam(required = false) String imageUrl,
                                 RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(name, category, price, stockQuantity, imageUrl);
            redirectAttributes.addFlashAttribute("success", "Them san pham thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam ProductCategory category,
                                 @RequestParam BigDecimal price,
                                 @RequestParam int stockQuantity,
                                 @RequestParam(required = false) String imageUrl,
                                 RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, name, category, price, stockQuantity, imageUrl);
            redirectAttributes.addFlashAttribute("success", "Cap nhat san pham thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleActive(id);
            redirectAttributes.addFlashAttribute("success", "Da thay doi trang thai san pham");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Da xoa san pham");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/addstock")
    public String addStock(@PathVariable Long id,
                            @RequestParam int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            productService.addStock(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Da nhap them " + quantity + " san pham");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
