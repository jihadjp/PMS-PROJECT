package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.*;
import com.jptechgenius.payroll.repository.*;
import com.jptechgenius.payroll.security.CustomUserDetails;
import com.jptechgenius.payroll.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [Employee Portal Controller]
 * -----------------------------
 * Ei controller ta shudhu Employee der jonno dedicated.
 * Ekhane employee ra tader dashboard dekhte pare, nijera attendance dite pare (Self Attendance),
 * Payslip download korte pare, ebong profile update korte pare.
 */
@Controller
@RequestMapping("/employee-portal")
public class EmployeePortalController {

    // 1. IP Validation Setup
    // application.properties file theke allowed IP list ta ekhane load hocche.
    @Value("${app.attendance.allowed-ips:127.0.0.1,0:0:0:0:0:0:0:1}") // Default Localhost
    private String allowedIpsString;

    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final ChargeSheetRepository chargeSheetRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // Constructor Injection (Sob service gula load kora holo)
    public EmployeePortalController(AttendanceRepository attendanceRepository,
                                    PayrollRepository payrollRepository,
                                    ChargeSheetRepository chargeSheetRepository,
                                    UserRepository userRepository,
                                    UserService userService) {
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.chargeSheetRepository = chargeSheetRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // ==========================================
    // 1. EMPLOYEE DASHBOARD
    // ==========================================
    /**
     * Ei method ta employee login korar por main dashboard page ta load kore.
     * Ekhane tar Salary History, Attendance Summary, Penalty sob kichu calculate kore pathano hoy.
     */
    @GetMapping("/dashboard")
    public String employeeDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails, // Current Logged in User
            Model model,
            @RequestParam(required = false) Integer month, // Filter er jonno month
            @RequestParam(required = false) Integer year) { // Filter er jonno year

        // Database theke user details anlam
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);

        // Safety Check: Jodi kono karone user na thake, login page e pathiye dibo.
        if (user == null || user.getEmployee() == null) {
            return "redirect:/login";
        }

        Employee me = user.getEmployee(); // Current Employee object
        LocalDate today = LocalDate.now();

        // Jodi user filter na kore, tahole current month show korbe
        int selectedMonth = (month != null) ? month : today.getMonthValue();
        int selectedYear = (year != null) ? year : today.getYear();

        // 1. Monthly Attendance Data Ana (Optimized Query)
        // Ager code e 'findAll' chilo ja slow. Ekhon direct month diye filter korchi.
        List<Attendance> monthlyAttendance = attendanceRepository.findByEmployeeIdAndMonth(me.getId(), selectedMonth, selectedYear);

        // 2. Present Days Count: Ei mase koto din present chilo ta count kora hocche.
        long presentDays = monthlyAttendance.stream()
                .filter(Attendance::isPresent)
                .count();

        // 3. Payslip History: Tar last 5 ta salary record dekhano hobe.
        // Ekhane amra dhorchi PayrollRepository te 'findTop5ByEmployeeIdOrderByPaymentDateDesc' ache,
        // Athoba stream use kore filter korchi (Safety r jonno stream rakhlam).
        List<PayrollRecord> myPayslips = payrollRepository.findAll().stream()
                .filter(r -> r.getEmployeeId().equals(me.getId()))
                .sorted((r1, r2) -> r2.getPaymentDate().compareTo(r1.getPaymentDate())) // Newest first
                .limit(5)
                .collect(Collectors.toList());

        // 4. Penalty Data
        List<ChargeSheet> myPenalties = chargeSheetRepository.findByEmployeeIdAndStatus(me.getId(), "PENDING");

        // 5. Today's Attendance (Fix: Handling Optional)
        Attendance todayAttendance = attendanceRepository.findByEmployeeIdAndDate(me.getId(), today)
                .orElse(null); // Jodi record na thake, null return korbe (Error dibe na)

        // Sob data HTML page e pathiye dilam
        model.addAttribute("employee", me);
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("myPayslips", myPayslips);
        model.addAttribute("myPenalties", myPenalties);
        model.addAttribute("attendance", todayAttendance);
        model.addAttribute("monthlyAttendance", monthlyAttendance);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);

        return "employee-portal/dashboard";
    }

    // ==========================================
    // 2. SELF ATTENDANCE (Check In / Check Out)
    // ==========================================
    /**
     * Ei method ta employee jokhon 'Check In' ba 'Check Out' button chapbe tokhon kaj korbe.
     * Ekhane IP Address check kora hoy, time calculation kora hoy, overtime hisab kora hoy.
     */
    @PostMapping("/mark-attendance")
    public String markAttendance(HttpServletRequest request, Principal principal, RedirectAttributes redirectAttributes) {

        // --- 1. IP SECURITY CHECK ---
        String remoteAddr = request.getRemoteAddr();
        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = remoteAddr;
        } else {
            clientIp = clientIp.split(",")[0].trim();
        }

        // Properties file theke IP list ene check korchi
        List<String> validIpList = Arrays.stream(allowedIpsString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        // Jodi IP match na kore, error message dibo.
        if (!validIpList.contains(clientIp)) {
            // Development er somoy IP check off rakhte paro, kintu production e on rakhbe.
            // redirectAttributes.addFlashAttribute("error", "SECURITY VIOLATION: Invalid IP: " + clientIp);
            // return "redirect:/employee-portal/dashboard";
            System.out.println("Warning: Access from unknown IP " + clientIp);
        }

        // --- 2. USER VALIDATION ---
        String username = principal.getName();
        User user = userService.getUserByUsername(username);

        if (user == null || user.getEmployee() == null) {
            redirectAttributes.addFlashAttribute("error", "Employee record not found!");
            return "redirect:/employee-portal/dashboard";
        }

        Employee employee = user.getEmployee();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check korchi ajker record ache kina (Fix: Optional handle kora)
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElse(null);

        // ==========================================
        // CASE A: CHECK IN (Jodi record na thake)
        // ==========================================
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEmployeeId(employee.getId());
            attendance.setDate(today);
            attendance.setCheckInTime(now);

            // Optimistic approach: Dhore nicchi se kaj korbe, tai 'CHECKED_IN' status dilam.
            // Final 'Present/Absent' status Check-Out er somoy decide hobe.
            attendance.setPresent(true);
            attendance.setStatus("CHECKED_IN");

            attendanceRepository.save(attendance);

            String formattedTime = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
            redirectAttributes.addFlashAttribute("success", "Good Morning! Check-In Successful at " + formattedTime);
        }

        // ==========================================
        // CASE B: CHECK OUT (Jodi CheckIn thake but CheckOut na thake)
        // ==========================================
        else if (attendance.getCheckOutTime() == null) {

            attendance.setCheckOutTime(now);

            // 1. Kajer somoy ber kora (Minutes -> Hours)
            long minutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), now);
            double workHours = minutes / 60.0;

            // Rounding to 2 decimal places (e.g., 8.25 hours)
            workHours = Math.round(workHours * 100.0) / 100.0;
            attendance.setWorkHours(workHours);

            // --- [CRITICAL LOGIC: 8 HOURS RULE] ---
            double standardWorkTime = 8.0;

            if (workHours >= standardWorkTime) {
                // SCENARIO 1: Full Day Work (>= 8 hours)
                attendance.setPresent(true);
                attendance.setStatus("PRESENT");

                // Overtime Calculation (Standard er upore ja kaj korbe tai Overtime)
                double ot = workHours - standardWorkTime;
                attendance.setOvertimeHours(Math.round(ot * 100.0) / 100.0);

                redirectAttributes.addFlashAttribute("success", "Checked Out! Full Day Counted. Work: " + workHours + " hrs.");
            } else {
                // SCENARIO 2: Short Work (< 8 hours) -> ABSENT or PARTIAL
                // Tomar requirement: 8 hours er niche hole Absent/Short Work.
                attendance.setPresent(false); // Salary pabe na (Partial salary logic PayrollService e thakbe)
                attendance.setStatus("SHORT_WORK"); // Dashboard e 'Short Work' dekhabe
                attendance.setOvertimeHours(0.0);

                redirectAttributes.addFlashAttribute("warning", "Checked Out. Warning: Less than 8 hours (" + workHours + " hrs). Marked as Short Work.");
            }

            attendanceRepository.save(attendance);
        }

        // ==========================================
        // CASE C: ALREADY COMPLETED
        // ==========================================
        else {
            redirectAttributes.addFlashAttribute("error", "You have already completed your workday!");
        }

        return "redirect:/employee-portal/dashboard";
    }

    // 3. Show Dispute Form Page
    @GetMapping("/dispute-absence")
    public String showDisputePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(user.getEmployee().getId(), today)
                .orElse(null);

        // Jodi record na thake ba Absent na hoy, tahole dashboard e ferot pathiye dibo
        if (attendance == null || !"ABSENT".equals(attendance.getStatus())) {
            return "redirect:/employee-portal/dashboard";
        }

        model.addAttribute("employee", user.getEmployee());
        model.addAttribute("attendance", attendance);
        return "employee-portal/dispute-absence";
    }

    // 4. Handle Dispute Submission
    @PostMapping("/dispute-absence")
    public String submitDispute(@RequestParam("reason") String reason,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(user.getEmployee().getId(), today)
                .orElse(null);

        if (attendance != null && "ABSENT".equals(attendance.getStatus())) {
            attendance.setDisputeReason(reason);
            attendance.setStatus("DISPUTE_OPEN"); // Status change, Admin notification pabe
            attendanceRepository.save(attendance);

            redirectAttributes.addFlashAttribute("success", "Dispute submitted successfully! Admin will review it.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Cannot dispute this record.");
        }

        return "redirect:/employee-portal/dashboard";
    }

    // ==========================================
    // 5. PROFILE & SETTINGS
    // ==========================================

    // Profile page dekhano
    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null && user.getEmployee() != null) {
            model.addAttribute("employee", user.getEmployee());
            model.addAttribute("user", user);
            return "employee-portal/profile";
        }
        return "redirect:/employee-portal/dashboard";
    }

    // Settings page dekhano
    @GetMapping("/settings")
    public String showSettings() {
        return "employee-portal/settings";
    }

    // Password Change Logic
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {

        String username = userDetails.getUsername();
        boolean isChanged = userService.changePassword(username, currentPassword, newPassword);

        if (isChanged) {
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
        }
        return "redirect:/employee-portal/settings";
    }
}