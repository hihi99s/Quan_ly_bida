package com.bida.controller.admin;

import com.bida.entity.HolidayCalendar;
import com.bida.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/holidays")
@RequiredArgsConstructor
public class AdminHolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public String listHolidays(Model model) {
        model.addAttribute("holidays", holidayService.getAllHolidays());
        return "admin/holidays";
    }

    @PostMapping
    public String createHoliday(@RequestParam String name,
                                 @RequestParam String date,
                                 @RequestParam(defaultValue = "false") boolean recurring,
                                 RedirectAttributes redirectAttributes) {
        try {
            holidayService.createHoliday(name, LocalDate.parse(date), recurring);
            redirectAttributes.addFlashAttribute("success", "Them ngay le thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/holidays";
    }

    @GetMapping("/{id}/edit")
    public String editHolidayRedirect() {
        return "redirect:/admin/holidays";
    }

    @PostMapping("/{id}/edit")
    public String updateHoliday(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String date,
                                 @RequestParam(defaultValue = "false") boolean recurring,
                                 RedirectAttributes redirectAttributes) {
        try {
            holidayService.updateHoliday(id, name, LocalDate.parse(date), recurring);
            redirectAttributes.addFlashAttribute("success", "Cap nhat ngay le thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/holidays";
    }

    @GetMapping("/{id}/delete")
    public String deleteHolidayRedirect() {
        return "redirect:/admin/holidays";
    }

    @PostMapping("/{id}/delete")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            holidayService.deleteHoliday(id);
            redirectAttributes.addFlashAttribute("success", "Da xoa ngay le");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/holidays";
    }
}
