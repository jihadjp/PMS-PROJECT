package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.Attendance;
import com.jptechgenius.payroll.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Service // Spring ke janano je eta ekta Service class
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // Constructor Injection: Database repository load korar jonno
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * [CHECK-OUT LOGIC]
     * Overtime automatic calculate hobe based on total duration.
     */
    @Transactional // Database update hobe, tai Transactional rakha valo
    public Attendance performCheckOut(Long employeeId) {

        // 1. Database theke ajker ACTIVE session khuje ber kora (jei tarikh-e check-in hoyeche)
        // Note: Amra dhore nicchi employee same day-te check-out korche.
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDateAndCheckOutTimeIsNull(employeeId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Error: No active check-in found for today! Please Check-In first."));

        // 2. Check-out time set kora
        LocalTime now = LocalTime.now();
        attendance.setCheckOutTime(now);

        // 3. Duration Calculate kora
        // Duration.between(start, end) time difference ber kore.
        long minutesWorked = Duration.between(attendance.getCheckInTime(), now).toMinutes();

        // Minutes ke Ghontay convert kora (Example: 90 min = 1.5 hours)
        double totalHours = minutesWorked / 60.0;

        // Decimal formatting (Optional): 2 doshomik porjonto rakha (e.g. 8.13 hours)
        totalHours = Math.round(totalHours * 100.0) / 100.0;

        // 4. Update Work Hours
        attendance.setWorkHours(totalHours);

        // 5. Overtime Calculation Magic
        double standardWorkHours = 8.0; // Standard Office Time

        if (totalHours > standardWorkHours) {
            // Case: Overtime hoyeche (e.g. 10 hours kaj korle 2 hours OT)
            double overtime = totalHours - standardWorkHours;

            // Rounding overtime to 2 decimal places
            overtime = Math.round(overtime * 100.0) / 100.0;

            attendance.setOvertimeHours(overtime);
        } else {
            // Case: Normal ba kom kaj hoyeche
            attendance.setOvertimeHours(0.0);
        }

        // 6. Status Update
        attendance.setStatus("CHECKED_OUT");

        // Finally save to Database
        return attendanceRepository.save(attendance);
    }
}