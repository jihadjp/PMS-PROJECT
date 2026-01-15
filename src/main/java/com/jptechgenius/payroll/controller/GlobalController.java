package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.Attendance;
import com.jptechgenius.payroll.repository.AttendanceRepository;
import com.jptechgenius.payroll.repository.ChargeSheetRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * [Global Controller Advice]
 * --------------------------
 * Ei class ta puro application er jonno ekta "Global Helper" hisebe kaj kore.
 * @ControllerAdvice annotation thakar karone, ei class er method gulo
 * sob Controller (EmployeeController, PayrollController etc.) theke access kora jay.
 * --- Kaj: ---
 * 1. Global Variables (App Name, Version) sob page e pathano.
 * 2. Global Exception Handling (File size error dhora).
 * 3. Global Data (Notification count) sob page e dekhano.
 */
@ControllerAdvice
public class GlobalController {

    // ==========================================
    // 1. CONFIGURATION VALUES
    // ==========================================
    // application.properties file theke value gulo inject kora hocche.
    // Ete kore nam change korte hole code e hat deya lagbe na, properties file change korlei hobe.

    @Value("${app.name}")
    private String appName;

    @Value("${app.team}")
    private String teamName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.support.email}")
    private String supportEmail;

    @Value("${app.support.phone}")
    private String supportPhone;

    // ==========================================
    // 2. MODEL ATTRIBUTES (Available in all HTML)
    // ==========================================
    // Ei method gulo run howar por, HTML page e ${appName}, ${teamName} use kora jabe.

    @ModelAttribute("appName")
    public String getAppName() { return appName; }

    @ModelAttribute("teamName")
    public String getTeamName() { return teamName; }

    @ModelAttribute("appVersion")
    public String getAppVersion() { return appVersion; }

    @ModelAttribute("supportEmail")
    public String getSupportEmail() { return supportEmail; }

    @ModelAttribute("supportPhone")
    public String getSupportPhone() { return supportPhone; }


    // ==========================================
    // 3. GLOBAL DATA FETCHING
    // ==========================================
    private final ChargeSheetRepository chargeSheetRepository;
    private final AttendanceRepository attendanceRepository;

    public GlobalController(ChargeSheetRepository chargeSheetRepository, AttendanceRepository attendanceRepository) {
        this.chargeSheetRepository = chargeSheetRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * [Pending Penalty Notification]
     * Header e je lal badge dekha jay (kotojon pending penalty ache),
     * sei songkha ta ekhane calculate kora hoy.
     * Safe Mode: Try-Catch use kora hoyeche jate database error holeo app crash na kore.
     */
    @ModelAttribute("globalPendingCount")
    public long getPendingPenaltyCount() {
        try {
            return chargeSheetRepository.findAll().stream()
                    .filter(c -> "PENDING".equals(c.getStatus())) // Sudhu pending gula count korbo
                    .count();
        } catch (Exception e) {
            System.err.println("Global Attribute Error (Penalty): " + e.getMessage());
            return 0; // Error hole 0 dekhabo
        }
    }

    /**
     * [Pending Dispute Notification]
     * Employee jodi kono attendance niye objog (Dispute) kore,
     * tokhon Admin er header-e lal badge e ei count ta dekhabe.
     */
    @ModelAttribute("globalDisputeCount")
    public long getDisputeCount() {
        try {
            // Attendance table check kore dekhchi koyta 'DISPUTE_OPEN' status ache
            return attendanceRepository.findAll().stream()
                    .filter(a -> "DISPUTE_OPEN".equals(a.getStatus()))
                    .count();
        } catch (Exception e) {
            System.err.println("Global Attribute Error (Dispute): " + e.getMessage());
            return 0;
        }
    }

    /**
     * [Today's Attendance Count]
     * Ajke kotojon employee present ache, sei songkha ta.
     * Etao header e ba dashboard e dekhano jete pare.
     */
    @ModelAttribute("globalPresentCount")
    public long getTodayAttendanceCount() {
        try {
            // Ajker tarikh diye filter kore shudhu present der count nilam
            return attendanceRepository.findByDate(LocalDate.now()).stream()
                    .filter(Attendance::isPresent)
                    .count();
        } catch (Exception e) {
            System.err.println("Global Attribute Error (Attendance): " + e.getMessage());
            return 0;
        }
    }

    // ==========================================
    // 4. GLOBAL EXCEPTION HANDLING
    // ==========================================

    /**
     * [File Size Error Handler]
     * Jodi keu 2MB er beshi chobi upload korte jay, tokhon Spring Boot error dey.
     * Ei method ta sei error ta dhore user ke sundor kore message dekhay,
     * bad ugly error page dekhay na.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {

        redirectAttributes.addFlashAttribute("error", "File is too large! Maximum allowed size is 2MB.");

        // Smart Redirect: User je page theke upload korchilo, oikhanei ferot pathabo.
        String referer = request.getHeader("Referer");

        return "redirect:" + (referer != null ? referer : "/employees");
    }
}