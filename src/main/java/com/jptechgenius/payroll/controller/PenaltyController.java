package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.ChargeSheet;
import com.jptechgenius.payroll.model.Employee;
import com.jptechgenius.payroll.repository.ChargeSheetRepository;
import com.jptechgenius.payroll.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * [Penalty Controller]
 * --------------------
 * Ei controller ta Employee der jarimana (Penalty/Fine) manage korar jonno.
 * Jemon: Kake koto taka fine kora holo, keno kora holo, sob hisab ekhane thake.
 */
@Controller
@RequestMapping("/penalty")
public class PenaltyController {

    private final EmployeeService employeeService;
    private final ChargeSheetRepository chargeSheetRepository;

    // Constructor Injection: Spring automatic service gulo load kore dibe.
    public PenaltyController(EmployeeService employeeService, ChargeSheetRepository chargeSheetRepository) {
        this.employeeService = employeeService;
        this.chargeSheetRepository = chargeSheetRepository;
    }

    // ==========================================
    // 1. ISSUE NEW PENALTY (Action)
    // ==========================================
    /**
     * [Issue Charge Sheet]
     * Admin jokhon modal form theke fine submit korbe, tokhon ei method call hobe.
     * Kaj:
     * 1. Employee khuje ber kora.
     * 2. Notun ChargeSheet record toiri kora.
     * 3. Status 'PENDING' set kora (jate porer masher betone kata jay).
     */
    @PostMapping("/issue")
    public String issueChargeSheet(@RequestParam Long employeeId,
                                   @RequestParam Double amount,
                                   @RequestParam String reason) {

        // Kon employee ke fine kora hocche ta ber korlam
        Employee employee = employeeService.getEmployeeById(employeeId);

        // Notun record create korchi
        ChargeSheet chargeSheet = new ChargeSheet();
        chargeSheet.setEmployee(employee);
        chargeSheet.setPenaltyAmount(amount);
        chargeSheet.setReason(reason);

        // Date ta 'Ajker Date' set kora holo ebong status 'PENDING' deya holo.
        // PENDING thakle Payroll generate korar somoy taka ta kata hobe.
        chargeSheet.setIssueDate(LocalDate.now());
        chargeSheet.setStatus("PENDING");

        // Database e save kora holo
        chargeSheetRepository.save(chargeSheet);

        // Kaj sesh hole list page e redirect korbo, jekhane notun entry ta dekha jabe
        return "redirect:/penalty/list";
    }

    // ==========================================
    // 2. VIEW PENALTY HISTORY
    // ==========================================
    /**
     * [View List]
     * Ekhane companyr sob penalty record eksathe dekhano hoy.
     * Ke kobe koto taka fine kheyeche, sob history ekhane thakbe.
     */
    @GetMapping("/list")
    public String viewPenaltyList(Model model) {
        // Database theke sob data niye aslam
        List<ChargeSheet> allCharges = chargeSheetRepository.findAll();

        // HTML page e data pathiye dilam table e dekhanor jonno
        model.addAttribute("charges", allCharges);

        return "penalty/list"; // penalty/list.html page load hobe
    }

    // ==========================================
    // 3. DELETE / REVOKE PENALTY
    // ==========================================
    /**
     * [Delete Penalty]
     * Jodi bhule kauke fine kora hoy, tahole eta delete kora jabe.
     * KINTU Condition ache: Jodi salary te taka ta already kata hoye jay (Status: DEDUCTED),
     * tahole ar delete kora jabe na. Sudhu 'PENDING' thaklei delete hobe.
     */
    @GetMapping("/delete/{id}")
    public String deletePenalty(@PathVariable Long id) {
        // ID diye record ta khuje ber korlam
        ChargeSheet cs = chargeSheetRepository.findById(id).orElse(null);

        // Safety Check: Shudhu 'PENDING' thaklei delete korbo.
        // Karon 'DEDUCTED' mane taka salary theke kete neya hoyeche, oita delete kora jabe na.
        if(cs != null && "PENDING".equals(cs.getStatus())) {
            chargeSheetRepository.delete(cs);
        }

        // Abar list page e ferot pathalam
        return "redirect:/penalty/list";
    }
}