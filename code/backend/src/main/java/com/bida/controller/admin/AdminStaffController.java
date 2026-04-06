package com.bida.controller.admin;

import com.bida.entity.Shift;
import com.bida.entity.StaffSchedule;
import com.bida.entity.User;
import com.bida.entity.enums.UserRole;
import com.bida.repository.UserRepository;
import com.bida.service.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffScheduleService scheduleService;
    private final UserRepository userRepository;

    @GetMapping
    public String staffPage(@RequestParam(required = false) String weekStart,
                             Model model) {
        LocalDate startOfWeek;
        if (weekStart != null && !weekStart.isEmpty()) {
            startOfWeek = LocalDate.parse(weekStart);
        } else {
            startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        }

        List<StaffSchedule> schedules = scheduleService.getSchedulesByWeek(startOfWeek);
        List<Shift> shifts = scheduleService.getAllShifts();
        List<User> staffUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STAFF && u.getActive())
                .toList();

        model.addAttribute("schedules", schedules);
        model.addAttribute("shifts", shifts);
        model.addAttribute("staffUsers", staffUsers);
        model.addAttribute("weekStart", startOfWeek);
        model.addAttribute("lateCheckIns", scheduleService.getLateCheckIns());

        // Generate 7 days
        List<LocalDate> weekDays = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(startOfWeek.plusDays(i));
        }
        model.addAttribute("weekDays", weekDays);

        return "admin/staff";
    }

    // ---- Shift CRUD ----

    @PostMapping("/shifts")
    public String createShift(@RequestParam String name,
                               @RequestParam String startTime,
                               @RequestParam String endTime,
                               RedirectAttributes redirectAttributes) {
        try {
            scheduleService.createShift(name, LocalTime.parse(startTime), LocalTime.parse(endTime), null);
            redirectAttributes.addFlashAttribute("success", "Them ca thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/shifts/{id}/delete")
    public String deleteShift(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteShift(id);
            redirectAttributes.addFlashAttribute("success", "Da xoa ca");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    // ---- Schedule ----

    @PostMapping("/schedule")
    public String assignSchedule(@RequestParam Long userId,
                                  @RequestParam Long shiftId,
                                  @RequestParam String date,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            scheduleService.assignSchedule(userId, shiftId, LocalDate.parse(date), principal.getName());
            redirectAttributes.addFlashAttribute("success", "Xep lich thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/schedule/{id}/checkin")
    public String checkIn(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.checkIn(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Check-in thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/schedule/{id}/checkout")
    public String checkOut(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.checkOut(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Check-out thanh cong");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/schedule/{id}/delete")
    public String deleteSchedule(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteSchedule(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Da xoa lich");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }
}
