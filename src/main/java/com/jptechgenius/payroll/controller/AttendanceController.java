package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.Attendance;
import com.jptechgenius.payroll.model.Employee;
import com.jptechgenius.payroll.repository.AttendanceRepository;
import com.jptechgenius.payroll.service.EmployeeService;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * [Attendance Controller]
 * -----------------------
 * Ei controller ta Employee der hajira (Attendance) manage korar jonno use kora hoy.
 * Ekhane attendance list dekha, notun attendance deya, sob handle kora hoy.
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final EmployeeService employeeService;
    private final AttendanceRepository attendanceRepository;

    // Constructor Injection: Database access er jonno Service ar Repository load kora holo.
    public AttendanceController(EmployeeService employeeService, AttendanceRepository attendanceRepository) {
        this.employeeService = employeeService;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * [DTO Class]
     * Ei choto class ta banano hoise shudhu HTML page e data pathanor subidhar jonno.
     * Ete Employee details ar tar status (Present/Absent) eksathe thake.
     * Database e eta save hoy na, just View er jonno.
     */
    @Data
    public static class DailyAttendanceDTO {
        private Employee employee;
        private String status; // Status: Present, Absent
        private Double overtime;
    }

    // ==========================================
    // 1. SHOW ATTENDANCE LIST (Daily Report)
    // ==========================================
    @GetMapping("/list")
    public String showAttendanceList(@RequestParam(value = "date", required = false) String dateStr, Model model) {

        // 1. Date Selection: User jodi kono date select na kore, tahole automatcally ajker date nibe.
        LocalDate date = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);

        // 2. Fetch Data: Database theke sob Employee ebong oi tarikh er Attendance record niye aslam.
        List<Employee> employees = employeeService.getAllEmployees();
        List<Attendance> attendances = attendanceRepository.findByDate(date);

        // Map Conversion: List theke Map e convert korlam jate Employee ID diye sohojei attendance khuje pawa jay.
        // Eta loop er vitore bar bar database call kora thekay.
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(Attendance::getEmployeeId, a -> a));

        List<DailyAttendanceDTO> reportList = new ArrayList<>();
        int presentCount = 0;
        int absentCount = 0;

        // 3. Loop Logic: Prottek Employee er jonno check korbo se ajke present kina.
        for (Employee emp : employees) {
            DailyAttendanceDTO dto = new DailyAttendanceDTO();
            dto.setEmployee(emp);

            // Check korchi ei employee er kono record map e ache kina
            if (attendanceMap.containsKey(emp.getId())) {
                Attendance att = attendanceMap.get(emp.getId());
                dto.setOvertime(att.getOvertimeHours());

                if (att.isPresent()) {
                    // Case A: Record ache abong 'Present' mark kora
                    dto.setStatus("Present");
                    presentCount++;
                } else {
                    // Case B: Record ache kintu 'Absent' mark kora (Admin manual vabe absent dise)
                    dto.setStatus("Absent");
                    absentCount++;
                }
            } else {
                // Case C: Kono record nai -> Tar mane Employee check-in kore nai -> So, ABSENT.
                dto.setStatus("Absent");
                dto.setOvertime(0.0);
                absentCount++; // Count e jog kore dilam
            }
            // Final list e data add korlam
            reportList.add(dto);
        }

        // 4. Pass Data: HTML page e sob data pathiye dilam jate table e show kora jay.
        model.addAttribute("attendanceList", reportList);
        model.addAttribute("selectedDate", date);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("totalCount", employees.size());

        return "attendance/list";
    }

    // ==========================================
    // 2. SHOW MARK ATTENDANCE FORM (Manual)
    // ==========================================
    @GetMapping("/mark")
    public String showMarkForm(@RequestParam(value = "date", required = false) String dateStr, Model model) {
        // Default vabe ajker date nibe
        LocalDate date = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);

        List<Employee> employees = employeeService.getAllEmployees();
        // Oi tarikh er joto hajira ache ta niye aslam
        List<Attendance> existingAttendances = attendanceRepository.findByDate(date);

        // Ekta Map banalam jate HTML page e checkbox gula agei 'Checked' dekhate pari jodi tara present thake.
        Map<Long, Attendance> attendanceMap = new HashMap<>();
        for (Attendance att : existingAttendances) {
            attendanceMap.put(att.getEmployeeId(), att);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("selectedDate", date);
        model.addAttribute("attendanceMap", attendanceMap);

        return "attendance/mark";
    }

    // ==========================================
    // 3. SAVE ATTENDANCE (Action)
    // ==========================================
    @PostMapping("/save")
    public String saveAttendance(@RequestParam Long employeeId,
                                 @RequestParam(defaultValue = "false") boolean isPresent,
                                 @RequestParam(defaultValue = "0") Double overtimeHours,
                                 @RequestParam String date) {

        LocalDate localDate = LocalDate.parse(date);

        // Check korchi ei date e ei employee er kono record age theke ache kina (Duplicate avoid korar jonno)
        Attendance existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, localDate)
                .orElse(null);

        if (existing != null) {
            // Record thakle just update korbo (Update Mode)
            existing.setPresent(isPresent);
            existing.setOvertimeHours(overtimeHours);

            // Status update kora better, nahole confusion thakbe
            if (isPresent) {
                existing.setStatus(existing.getStatus().equals("CHECKED_OUT") ? "CHECKED_OUT" : "PRESENT_MANUAL");
            } else {
                existing.setStatus("ABSENT");
            }

            attendanceRepository.save(existing);
        } else {
            // Record na thakle notun record create korbo (Create Mode)
            Attendance newAtt = new Attendance();
            newAtt.setEmployeeId(employeeId);
            newAtt.setDate(localDate);
            newAtt.setPresent(isPresent);
            newAtt.setOvertimeHours(overtimeHours);

            // Default status set kora
            newAtt.setStatus(isPresent ? "PRESENT_MANUAL" : "ABSENT");

            attendanceRepository.save(newAtt);
        }

        // Kaj sesh hole abar oi date er page ei thakbo
        return "redirect:/attendance/mark?date=" + date;
    }
}