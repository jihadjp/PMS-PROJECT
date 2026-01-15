package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.Attendance;
import com.jptechgenius.payroll.model.Employee;
import com.jptechgenius.payroll.repository.AttendanceRepository;
import com.jptechgenius.payroll.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/disputes")
public class AdminDisputeController {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AdminDisputeController(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    // 1. Show Dispute List
    @GetMapping
    public String viewDisputes(Model model) {
        // Shudhu jeigula Dispute Open kora ache segula anbo
        List<Attendance> disputes = attendanceRepository.findByStatus("DISPUTE_OPEN");

        // Employee der nam janar jonno ekta Map toiri korchi (ID -> Employee Object)
        // Karon Attendance table e shudhu ID ache, Nam nai.
        Map<Long, Employee> employeeMap = new HashMap<>();
        for (Attendance att : disputes) {
            if (!employeeMap.containsKey(att.getEmployeeId())) {
                employeeRepository.findById(att.getEmployeeId())
                        .ifPresent(emp -> employeeMap.put(emp.getId(), emp));
            }
        }

        model.addAttribute("disputes", disputes);
        model.addAttribute("employeeMap", employeeMap);

        return "admin/dispute-list";
    }

    // 2. Handle Decision (Accept/Reject)
    @PostMapping("/resolve")
    public String resolveDispute(@RequestParam Long id,
                                 @RequestParam String action,
                                 RedirectAttributes redirectAttributes) {

        Attendance att = attendanceRepository.findById(id).orElse(null);

        if (att == null) {
            redirectAttributes.addFlashAttribute("error", "Record not found!");
            return "redirect:/admin/disputes";
        }

        if ("ACCEPT".equals(action)) {
            // Admin mene niyeche: TAKE PRESENT KORE DAO
            att.setPresent(true);
            att.setStatus("PRESENT_MANUAL"); // Status update
            att.setDisputeReason(att.getDisputeReason() + " [ACCEPTED BY ADMIN]");
            // Note: Tumi chaile ekhane manually 8 hours set kore dite paro
            att.setWorkHours(8.0);

            redirectAttributes.addFlashAttribute("success", "Dispute Accepted. Employee marked as PRESENT.");
        } else {
            // Admin mane nai: ABSENT E THAKBE
            att.setPresent(false);
            att.setStatus("ABSENT");
            att.setDisputeReason(att.getDisputeReason() + " [REJECTED BY ADMIN]");

            redirectAttributes.addFlashAttribute("error", "Dispute Rejected. Marked as ABSENT.");
        }

        attendanceRepository.save(att);
        return "redirect:/admin/disputes";
    }
}