package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.PayrollRecord;
import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.service.PayrollService;
import com.jptechgenius.payroll.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * [Payroll Controller]
 * --------------------
 * Ei controller ta Salary related sob kaj handle kore.
 * Jemon: Notun salary toiri kora (Generate), sob list dekha (Sheet),
 * ar individual employee er payslip dekha.
 */
@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final UserService userService;

    // Constructor Injection: Spring automatic service gulo load kore dibe.
    public PayrollController(PayrollService payrollService, UserService userService) {
        this.payrollService = payrollService;
        this.userService = userService;
    }

    /**
     * [Show Generate Page]
     * Admin jokhon 'Run Payroll' button chapbe, tokhon ei page ashbe.
     * Ekhane Month ar Year select korar option thake.
     */
    @GetMapping("/generate")
    public String showGeneratePage() {
        return "payroll/generate";
    }

    /**
     * [Process Payroll]
     * User Month/Year select kore submit korle ekhane ashbe.
     * Service layer call kore oi masher salary calculate kora hobe,
     * tarpor Salary Sheet page e redirect kora hobe.
     */
    @PostMapping("/process")
    public String processPayroll(@RequestParam("month") int month, @RequestParam("year") int year) {
        payrollService.generateMonthlyPayroll(month, year);
        return "redirect:/payroll/sheet?month=" + month + "&year=" + year;
    }

    /**
     * [Show Salary Sheet]
     * Ekhane generated salary gulor list dekhano hoy.
     * Filter Logic: Jodi user kono mash select na kore, tahole automatic
     * 'Current Month' er data dekhabe.
     */
    @GetMapping("/sheet")
    public String showSalarySheet(Model model,
                                  @RequestParam(required = false) Integer month,
                                  @RequestParam(required = false) Integer year) {

        // Jodi parameter na thake, tobe ajker tarikh theke mash/bochor ber korbo
        if (month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        // Oi masher record gulo database theke ana holo
        List<PayrollRecord> records = payrollService.getRecordsByMonthAndYear(month, year);

        // Data HTML e pathano holo
        model.addAttribute("records", records);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        return "payroll/sheet";
    }

    /**
     * [View Payslip - SECURITY CHECK]
     * Ei method ta diye ekta single Payslip dekha jay.
     * Ekhane ekta kothin security check ache jate ekjon employee onnojoner salary dekhte na pare.
     */
    @GetMapping("/payslip/{id}")
    public String viewPayslip(@PathVariable("id") String idString, Model model, Principal principal) {

        // 1. ID VALIDATION CHECK:
        // User jodi URL e 'bhul' string dey (Jemon: /payslip/123),
        // tokhon jate server crash (400 Bad Request) na kore, tai String hisebe nilam.
        UUID id;
        try {
            id = UUID.fromString(idString); // Ekhane String theke UUID bananor chesta
        } catch (IllegalArgumentException e) {
            // Jodi ID valid na hoy, user ke Salary Sheet page e pathiye dibo
            System.out.println("Error: Invalid UUID format provided - " + idString);
            return "redirect:/payroll/sheet?error=InvalidID";
        }

        // 2. Payslip record ta database theke anlam
        // Ekhon ar kono voy nei karon 'id' ta valid UUID
        PayrollRecord record = payrollService.getRecordById(id);

        // 3. Je user login kore ache, tar details ber korlam (Username diye)
        User currentUser = userService.getUserByUsername(principal.getName());

        // 4. SECURITY LOGIC:
        // Jodi User 'ADMIN' ba 'SUPER_ADMIN' hoy -> Dekhte parbe.
        // Jodi User 'EMPLOYEE' hoy, kintu Payslip ta tar nijer hoy -> Dekhte parbe.
        // Kintu jodi Employee onno karo Payslip dekhte chay -> Access Denied.

        boolean isAdmin = currentUser.getRole().equals("ADMIN") || currentUser.getRole().equals("SUPER_ADMIN");

        // Own Payslip kina check kori
        boolean isOwnPayslip = false;

        // Jodi employee object null na hoy, tobe id check korbo (NullPointer fix)
        if (currentUser.getEmployee() != null) {
            isOwnPayslip = record.getEmployeeId().equals(currentUser.getEmployee().getId());
        }

        // Jodi Admin na hoy EBONG nijer payslip na hoy, tahole Access Denied
        if (!isAdmin && !isOwnPayslip) {
            return "redirect:/access-denied";
        }

        // Sob thik thakle payslip page show korbo
        model.addAttribute("record", record);
        return "payroll/payslip";
    }
}