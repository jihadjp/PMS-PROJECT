package com.jptechgenius.payroll.service.impl;

import com.jptechgenius.payroll.model.*;
import com.jptechgenius.payroll.repository.*;
import com.jptechgenius.payroll.service.PayrollService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * [Payroll Calculation Engine]
 * ----------------------------
 * Ei class tai holo puro project er main engine.
 * Masher sheshe Admin jokhon "Run Payroll" button chape, tokhon ei class er kaj shuru hoy.
 * Eta prottek employee er hajira, overtime, penalty, tax sob check kore salary toiri kore.
 */
@Service
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final ChargeSheetRepository chargeSheetRepository;

    // Constructor Injection (Sob dependency load korlam)
    public PayrollServiceImpl(EmployeeRepository empRepo, PayrollRepository payRepo,
                              AttendanceRepository attRepo, ChargeSheetRepository csRepo) {
        this.employeeRepository = empRepo;
        this.payrollRepository = payRepo;
        this.attendanceRepository = attRepo;
        this.chargeSheetRepository = csRepo;
    }

    /**
     * [Generate Monthly Payroll]
     * Etai asol magic method.
     * 1. Koto din office khola chilo ber kore.
     * 2. Ke koydin ashche ber kore.
     * 3. Overtime, Penalty, Tax jog-biyog kore Final salary banay.
     */
    @Override
    @Transactional // Transactional mane: Majhpothe error hole puro process cancel (rollback) hobe.
    public void generateMonthlyPayroll(int month, int year) {

        // Prothome sob Active employee der list nilam
        List<Employee> employees = employeeRepository.findAll();

        // 1. Masher suru ar shesh tarikh ber korlam (Example: Nov 1 to Nov 30)
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // FIX: Asol karjodibos (Working Days) count kora (Friday baad diye)
        // Amra dhore nicchi Friday holo weekly holiday.
        int actualWorkingDaysCount = 0;
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                actualWorkingDaysCount++;
            }
        }

        // Safety check: Jodi working day 0 hoy (osombhov, tobuo check), tahole kaj korbe na.
        if (actualWorkingDaysCount == 0) {
            System.err.println("ERROR: No working days found. Skipping payroll.");
            return;
        }

        // Prottek employee er jonno loop chalabo
        for (Employee emp : employees) {

            // 1. Jodi employee 'SUSPENDED' thake, take salary dibo na. Skip korbo.
            if ("SUSPENDED".equalsIgnoreCase(emp.getStatus())) {
                continue;
            }

            // 2. Duplicate Check:
            // Jodi admin bhul kore ek e masher salary dui bar generate kore,
            // tahole ager record ta delete kore notun ta banabo (Clean Slate).
            List<PayrollRecord> oldRecords = payrollRepository.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year);
            if (!oldRecords.isEmpty()) {
                payrollRepository.deleteAll(oldRecords);
            }

            // 3. Notun salary record toiri shuru
            PayrollRecord record = new PayrollRecord();

            // --- SALARY CALCULATION LOGIC ---

            double basicSalary = emp.getBasicSalary(); // Tar mul beton

            // Daily Rate: Ek din kaj korle koto taka pay?
            // Formula: Basic / Total Working Days
            double dailyRate = basicSalary / actualWorkingDaysCount;

            // [FIXED] Hourly Rate Ber Kora (Overtime er jonno)
            // Amra dhorchi office 8 ghonta chole.
            double hourlyRate = dailyRate / 8.0;

            // Attendance Data Ana:
            List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndMonth(emp.getId(), month, year);

            int actualPresentDays = 0;
            double totalOvertimeHours = 0;

            // Loop chaliye dekhi se asole koydin present chilo
            for (Attendance att : attendanceList) {
                // FIX: Shudhu working day te present thaklei count hobe.
                // Friday te kaj korle seta overtime e jabe, regular day te na.
                if (att.isPresent() && att.getDate().getDayOfWeek() != DayOfWeek.FRIDAY) {
                    actualPresentDays++;
                }

                // Attendance record theke overtime hour jog korchi
                // (Friday te kaj korle purotai overtime hisebe ekhane jog hobe)
                totalOvertimeHours += att.getOvertimeHours();
            }

            // Payable Basic: Je koydin kaj koreche, tar taka.
            double payableBasic = dailyRate * actualPresentDays;

            // ==========================================
            // [UPDATED] DYNAMIC OVERTIME LOGIC
            // ==========================================
            // Fix: Database e 0.0 thakle problem. Tai amra auto calculate korchi.
            // Niyom: Regular hourly rate er 1.5 gun hobe overtime rate.
            // Example: Ghontay 100 taka hole, overtime e pabe 150 taka.
            double overtimeRate = hourlyRate * 1.5;

            // Final Calculation: Total Hours x Dynamic Rate
            double overtimePay = totalOvertimeHours * overtimeRate;


            // --- DEDUCTION LOGIC (Taka Kata) ---

            // A. Penalty / Fine:
            // Check korchi ei mase tar name kono Charge Sheet ache kina.
            List<ChargeSheet> monthlyCharges = chargeSheetRepository.findByEmployeeIdAndIssueDateBetween(emp.getId(), startDate, endDate);
            double totalPenaltyForMonth = 0;

            for (ChargeSheet charge : monthlyCharges) {
                totalPenaltyForMonth += charge.getPenaltyAmount();

                // Penalty kete neyar por status 'DEDUCTED' kore dibo jate porer mase abar na kate.
                if ("PENDING".equals(charge.getStatus())) {
                    charge.setStatus("DEDUCTED");
                    chargeSheetRepository.save(charge);
                }
            }

            // B. Tax:
            // Payable amount er upor 5% tax kata hobe (Jodi policy thake).
            double tax = payableBasic * 0.05; // 5% Tax

            // C. Fixed Deductions: (Jemon lunch bill, transport charge etc.)
            double fixedDeduction = (emp.getDeductions() != null) ? emp.getDeductions() : 0;

            // Total koto kata gelo?
            double totalDeductions = tax + totalPenaltyForMonth + fixedDeduction;

            // --- FINAL NET PAY ---
            // Formula: (Kajera Taka + Overtime) - (Sob KataKuti)
            double netPay = payableBasic + overtimePay - totalDeductions;

            // Doshomik sonkha sundor (Round) korar jonno logic
            payableBasic = Math.round(payableBasic * 100.0) / 100.0;
            overtimePay = Math.round(overtimePay * 100.0) / 100.0;
            totalDeductions = Math.round(totalDeductions * 100.0) / 100.0;
            netPay = Math.round(netPay * 100.0) / 100.0;

            // --- SAVING DATA ---
            // Snapshot nicchi: Employee er nam/podobi save rakhchi.
            record.setEmployeeId(emp.getId());
            record.setEmployeeName(emp.getName());
            record.setDesignation(emp.getDesignation());
            record.setImageUrl(emp.getImageUrl()); // Payslip e chobi dekhanor jonno

            record.setMonth(month);
            record.setYear(year);

            record.setBasicSalary(payableBasic); // Eita mul basic na, eita holo "Payable Basic"
            record.setBonus(overtimePay);
            record.setDeductions(totalDeductions);
            record.setNetPay(netPay);

            record.setPaymentDate(LocalDate.now());

            // Finally database e save kora holo
            payrollRepository.save(record);
        }
    }

    // --- Helper Methods ---

    @Override
    public List<PayrollRecord> getRecordsByMonthAndYear(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }

    @Override
    public List<PayrollRecord> getAllRecords() {
        return payrollRepository.findAll();
    }

    @Override
    public PayrollRecord getRecordById(UUID id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Sorry no salary slip found for this id " + id
                ));
    }
}