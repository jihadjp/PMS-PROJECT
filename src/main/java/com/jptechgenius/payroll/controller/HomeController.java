package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.ChargeSheet;
import com.jptechgenius.payroll.model.PayrollRecord;
import com.jptechgenius.payroll.repository.AttendanceRepository;
import com.jptechgenius.payroll.repository.ChargeSheetRepository;
import com.jptechgenius.payroll.repository.EmployeeRepository;
import com.jptechgenius.payroll.repository.PayrollRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [Home Controller - The Dashboard Engine]
 * -----------------------------------------
 * Ei class ta holo amader Application er main entry point.
 * Jokhon keu 'localhost:9090' te dhukbe, tokhon ei controller tai call hobe.
 * Eita muloto Dashboard er sob graph, chart, ar count (Total Employee, Salary etc.) calculate kore.
 */
@Controller
public class HomeController {

    // Database theke data anar jonno Repository gulo lagbe
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final ChargeSheetRepository chargeSheetRepository;
    private final AttendanceRepository attendanceRepository;

    // Constructor Injection: Spring Boot automatic repository gulo inject kore dibe.
    public HomeController(EmployeeRepository employeeRepository,
                          PayrollRepository payrollRepository,
                          ChargeSheetRepository chargeSheetRepository,
                          AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.chargeSheetRepository = chargeSheetRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * [Dashboard Page Logic]
     * URL: http://localhost:9090/
     * Kaj: Database theke sob data ene calculate kore Dashboard HTML e pathano.
     */
    @GetMapping("/")
    public String index(Model model) {

        // ==========================================
        // STEP 1: Default Value Setup (Safe Mode)
        // ==========================================
        // Amra shurutei sob variable 0 ba empty list diye initialize korchi.
        // Karon: Jodi kono karone database connect na hoy ba data na thake,
        // tobuo jeno 'NullPointerException' na khay ebong Dashboard ta atleast open hoy.
        long totalEmployees = 0;
        double totalPayroll = 0.0;
        long absentToday = 0;
        int pendingPenaltiesCount = 0;
        List<ChargeSheet> recentPenalties = new ArrayList<>();
        double[] monthlyData = new double[12]; // Chart er jonno 12 masher array (sob 0.0)

        try {
            // ==========================================
            // STEP 2: Real Data Fetching
            // ==========================================

            // A. Total Employees: Database e koyjon employee ache gunlam.
            totalEmployees = employeeRepository.count();

            // B. Total Salary Expense:
            // Sob payroll record ene tader 'NetPay' jog kore dekhlam companyr total koto khoroch hoise.
            List<PayrollRecord> payrolls = payrollRepository.findAll();
            totalPayroll = payrolls.stream().mapToDouble(PayrollRecord::getNetPay).sum();

            // C. Absent Count Logic:
            // Prothome dekhlam ajke koyjon 'Present' ache.
            long presentCount = attendanceRepository.findByDate(LocalDate.now()).stream()
                    .filter(a -> a.isPresent())
                    .count();
            // Total Employee theke Present baad dilei Absent pawa jabe.
            // Math.max(0, ...) deya hoise jate vul koreo negative sonkha na ashe.
            absentToday = Math.max(0, totalEmployees - presentCount);

            // D. Penalty / Charge Sheet Logic:
            List<ChargeSheet> allCharges = chargeSheetRepository.findAll();

            // Sudhu 'PENDING' status er penalty gula filter kore nilam.
            List<ChargeSheet> pendingList = allCharges.stream()
                    .filter(c -> "PENDING".equals(c.getStatus()))
                    .collect(Collectors.toList());

            pendingPenaltiesCount = pendingList.size(); // Dashboard er lal card e dekhabo

            // Recent 5 ta penalty nilam (Date onujayi sort kore) table e dekhanor jonno.
            recentPenalties = pendingList.stream()
                    .sorted((c1, c2) -> c2.getIssueDate().compareTo(c1.getIssueDate()))
                    .limit(5)
                    .collect(Collectors.toList());

            // E. Chart Data Calculation (Jan - Dec):
            // Loop chaliye check korchi kon record kon masher, sei onujayi array te salary jog korchi.
            int currentYear = LocalDate.now().getYear();
            for (PayrollRecord rec : payrolls) {
                if (rec.getYear() == currentYear) {
                    int monthIndex = rec.getMonth() - 1; // Array index 0 theke shuru hoy, tai -1 kora lagbe.
                    if (monthIndex >= 0 && monthIndex < 12) {
                        monthlyData[monthIndex] += rec.getNetPay();
                    }
                }
            }

        } catch (Exception e) {
            // Jodi database e kono oshubidha hoy, tahole ekhane error catch korbe.
            // App crash korbe na, just console e error ta dekhabe.
            System.err.println("Dashboard Data Error: " + e.getMessage());
            e.printStackTrace();
        }

        // ==========================================
        // STEP 3: Send Data to HTML
        // ==========================================
        // Model er maddhome data gula 'index.html' page e pathiye dilam.
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalPayroll", totalPayroll);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("pendingPenalties", pendingPenaltiesCount);
        model.addAttribute("recentPenalties", recentPenalties);
        model.addAttribute("monthlyPayrollData", monthlyData);
        model.addAttribute("location", "Dhaka, Bangladesh"); // Static location

        return "dashboard/index";
    }

    // --- Static Pages ---

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/help")
    public String helpPage() {
        return "help";
    }

    @GetMapping("/home")
    public String home() {
        return "index";
    }

}