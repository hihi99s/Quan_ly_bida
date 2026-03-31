package com.bida.controller.admin;

import com.bida.entity.DiscountCode;
import com.bida.service.DiscountCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/discount-codes")
@RequiredArgsConstructor
public class AdminDiscountCodeController {

    private final DiscountCodeService discountCodeService;

    @GetMapping
    public String listCodes(Model model) {
        model.addAttribute("codes", discountCodeService.getAllCodes());
        return "admin/discount-codes";
    }

    @PostMapping
    public String createCode(@RequestParam String code,
                             @RequestParam BigDecimal discountPercent,
                             @RequestParam(required = false) Integer maxUsageCount,
                             @RequestParam(required = false) String expiryDateStr,
                             RedirectAttributes redirectAttributes) {
        try {
            LocalDate expiryDate = null;
            if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                expiryDate = LocalDate.parse(expiryDateStr);
            }
            discountCodeService.createCode(code, discountPercent, maxUsageCount, expiryDate);
            redirectAttributes.addFlashAttribute("success", "Da them ma giam gia: " + code);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Loi: " + e.getMessage());
        }
        return "redirect:/admin/discount-codes";
    }

    @PostMapping("/{id}/edit")
    public String updateCode(@PathVariable Long id,
                             @RequestParam String code,
                             @RequestParam BigDecimal discountPercent,
                             @RequestParam(required = false) Integer maxUsageCount,
                             @RequestParam(required = false) String expiryDateStr,
                             RedirectAttributes redirectAttributes) {
        try {
            LocalDate expiryDate = null;
            if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                expiryDate = LocalDate.parse(expiryDateStr);
            }
            discountCodeService.updateCode(id, code, discountPercent, maxUsageCount, expiryDate);
            redirectAttributes.addFlashAttribute("success", "Da cap nhat ma giam gia");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Loi: " + e.getMessage());
        }
        return "redirect:/admin/discount-codes";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            discountCodeService.toggleActive(id);
            redirectAttributes.addFlashAttribute("success", "Da thay doi trang thai");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Loi: " + e.getMessage());
        }
        return "redirect:/admin/discount-codes";
    }

    @PostMapping("/{id}/delete")
    public String deleteCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            discountCodeService.deleteCode(id);
            redirectAttributes.addFlashAttribute("success", "Da xoa ma giam gia");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Loi: " + e.getMessage());
        }
        return "redirect:/admin/discount-codes";
    }
}
