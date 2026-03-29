package com.bida.controller;

import com.bida.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TableService tableService;
    private final AppSettingService appSettingService;
    private final HolidayService holidayService;
    private final ProductService productService;
    private final CustomerService customerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        model.addAttribute("holidayMode", appSettingService.isHolidayMode());

        // Phase 2: Holiday calendar check
        boolean isCalendarHoliday = holidayService.isHoliday(java.time.LocalDate.now());
        model.addAttribute("isCalendarHoliday", isCalendarHoliday);

        // Phase 2: Products for order tab
        model.addAttribute("products", productService.getActiveProducts());

        // Phase 2: Customers for search
        model.addAttribute("customers", customerService.getAllCustomers());

        return "dashboard";
    }
}
