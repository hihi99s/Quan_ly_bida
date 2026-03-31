package com.bida.controller.admin;

import com.bida.entity.enums.TableType;
import com.bida.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tables")
@RequiredArgsConstructor
public class AdminTableController {

    private final TableService tableService;

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        model.addAttribute("tableTypes", TableType.values());
        return "admin/tables";
    }

    @PostMapping
    public String createTable(@RequestParam String name,
                              @RequestParam TableType tableType,
                              RedirectAttributes ra) {
        try {
            tableService.createTable(name, tableType);
            ra.addFlashAttribute("success", "Đã thêm bàn: " + name);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/edit")
    public String updateTable(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam TableType tableType,
                              RedirectAttributes ra) {
        try {
            tableService.updateTable(id, name, tableType);
            ra.addFlashAttribute("success", "Đã cập nhật bàn");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/disable")
    public String disableTable(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tableService.disableTable(id);
            ra.addFlashAttribute("success", "Đã ngừng hoạt động bàn");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/enable")
    public String enableTable(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tableService.enableTable(id);
            ra.addFlashAttribute("success", "Đã kích hoạt lại bàn");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/{id}/delete")
    public String deleteTable(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tableService.deleteTable(id);
            ra.addFlashAttribute("success", "Đã xóa bàn");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tables";
    }
}
