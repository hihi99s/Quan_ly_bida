package com.bida.controller.admin;

import com.bida.entity.PriceRule;
import com.bida.entity.enums.DayType;
import com.bida.entity.enums.TableType;
import com.bida.service.AppSettingService;
import com.bida.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/prices")
@RequiredArgsConstructor
public class AdminPriceController {

    private final PriceRuleService priceRuleService;
    private final AppSettingService appSettingService;

    // ─── GET: Trang quản lý giá ─────────────────────────────────────────────────

    @GetMapping
    public String pricePage(Model model,
                            @RequestParam(defaultValue = "POOL") String tableType) {
        TableType type;
        try {
            type = TableType.valueOf(tableType.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = TableType.POOL;
        }

        Map<DayType, List<PriceRule>> grouped = priceRuleService.getRulesGroupedByDay(type);

        model.addAttribute("selectedType", type);
        model.addAttribute("tableTypes", TableType.values());
        model.addAttribute("dayTypes", DayType.values());
        model.addAttribute("rulesMap", grouped);
        model.addAttribute("holidayMode", appSettingService.isHolidayMode());

        return "admin/prices";
    }

    // ─── POST: Thêm mới rule giá ─────────────────────────────────────────────────

    @PostMapping
    public String createRule(@RequestParam TableType tableType,
                             @RequestParam DayType dayType,
                             @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                             @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                             @RequestParam BigDecimal pricePerHour,
                             RedirectAttributes ra) {
        try {
            priceRuleService.createRule(tableType, dayType, startTime, endTime, pricePerHour);
            ra.addFlashAttribute("success", "✓ Đã thêm bảng giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "✗ Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prices?tableType=" + tableType;
    }

    // ─── POST: Cập nhật rule giá ─────────────────────────────────────────────────

    @PostMapping("/{id}/edit")
    public String updateRule(@PathVariable Long id,
                             @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                             @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                             @RequestParam BigDecimal pricePerHour,
                             @RequestParam String tableType,
                             RedirectAttributes ra) {
        try {
            priceRuleService.updateRule(id, startTime, endTime, pricePerHour);
            ra.addFlashAttribute("success", "✓ Đã cập nhật bảng giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "✗ Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prices?tableType=" + tableType;
    }

    // ─── POST: Xóa rule giá ──────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteRule(@PathVariable Long id,
                             @RequestParam String tableType,
                             RedirectAttributes ra) {
        try {
            priceRuleService.deleteRule(id);
            ra.addFlashAttribute("success", "✓ Đã xóa bảng giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "✗ Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/prices?tableType=" + tableType;
    }

    // ─── POST: Toggle Holiday Mode ───────────────────────────────────────────────

    @PostMapping("/holiday/toggle")
    public String toggleHoliday(@RequestParam(defaultValue = "POOL") String tableType,
                                RedirectAttributes ra) {
        appSettingService.toggleHolidayMode();
        boolean mode = appSettingService.isHolidayMode();
        ra.addFlashAttribute("success",
                "Holiday Mode: " + (mode ? "🔴 ĐÃ BẬT — Áp dụng giá ngày lễ" : "⚫ ĐÃ TẮT — Giá bình thường"));
        return "redirect:/admin/prices?tableType=" + tableType;
    }
}
