package com.jptechgenius.payroll.service.impl;

import com.jptechgenius.payroll.model.Employee;
import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.repository.*;
import com.jptechgenius.payroll.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * [Employee Service Implementation]
 * ---------------------------------
 * Ei class-ta holo Employee Management er "Brain" ba logic layer.
 * Controller sorasori database e hat dey na, ei service er maddhome kaj kore.
 * Ekhane notun employee add kora, delete kora, search kora, ebong auto-login account toiri korar logic ache.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    // Ei repository gula lagbe karon employee delete korar somoy
    // tar attendance, salary history, penalty sob kichu delete korte hobe.
    private final ChargeSheetRepository chargeSheetRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;

    // Login account create korar jonno user repository lagbe
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection (Sob dependency load kora holo)
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               ChargeSheetRepository chargeSheetRepository,
                               AttendanceRepository attendanceRepository,
                               PayrollRepository payrollRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.chargeSheetRepository = chargeSheetRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public List<Employee> searchEmployees(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            // Name er moddhe keyword khuje ber korbe (Case Insensitive)
            return employeeRepository.findByNameContainingIgnoreCase(keyword);
        }
        return employeeRepository.findAll();
    }

    /**
     * [Save Employee Logic]
     * Ei method ta Create ebong Update duita kaj e kore.
     * Special Feature: Jokhon prothombar employee create hoy,
     * tokhon automatic tar jonno ekta Login ID (User) toiri hoye jay.
     */
    @Override
    @Transactional // Transactional mane puro kaj ta ekbare hobe, nahole rollback hobe.
    public void saveEmployee(Employee employee) {

        // 1. Check korchi eta ki notun employee naki purono (Update)?
        // ID null thaka mane notun.
        boolean isNewEntry = (employee.getId() == null);

        // Status na thakle default 'ACTIVE' set kore dicchi.
        if (employee.getStatus() == null) {
            employee.setStatus("ACTIVE");
        }

        // Database e Employee save kora holo
        Employee savedEmployee = employeeRepository.save(employee);

        // 2. AUTOMATION: Auto User Account Creation
        // Jodi notun employee hoy, tahole tar jonno login account banabo.
        if (isNewEntry) {
            User newUser = new User();

            // Username selection logic:
            // Jodi Email thake, tobe Email-i username hobe.
            // Email na thakle Phone Number hobe username.
            String username = (employee.getEmail() != null && !employee.getEmail().isEmpty())
                    ? employee.getEmail()
                    : employee.getPhoneNumber();

            newUser.setUsername(username);
            newUser.setFullName(employee.getName());
            newUser.setEmail(employee.getEmail()); // Password recovery er jonno email set korlam
            newUser.setPassword(passwordEncoder.encode("1234")); // Default password '1234'
            newUser.setRole("EMPLOYEE"); // Role hobe sadharon Employee
            newUser.setEmployee(savedEmployee); // User table er sathe Employee table link kore dilam
            newUser.setImageUrl(employee.getImageUrl()); // Profile picture set kora holo

            // User table e save kora holo
            userRepository.save(newUser);
        }
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found for id :: " + id));
    }

    /**
     * [Delete Employee Logic]
     * Employee delete kora ekta sensitive kaj.
     * Sorasori delete korle database error dibe (Foreign Key Constraint).
     * Tai age tar sob history (Attendance, Salary, Fine) delete korte hoy.
     */
    @Override
    @Transactional
    public void deleteEmployeeById(Long id) {
        // 1. Clean up functional records (History clear kora)
        chargeSheetRepository.deleteByEmployeeId(id);
        attendanceRepository.deleteByEmployeeId(id);
        payrollRepository.deleteByEmployeeId(id);

        // 2. Delete Associated User Account
        // Employee delete korle tar login access o delete kora uchit.
        // Amra check korchi oi employee er kono user account ache kina.
        User associatedUser = userRepository.findByEmployeeId(id);
        if (associatedUser != null) {
            userRepository.delete(associatedUser);
        }

        // 3. Finally, delete the employee record
        employeeRepository.deleteById(id);
    }

    /**
     * [Update Status Logic]
     * Employee ke Suspend ba Activate korar jonno ei method use hoy.
     */
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        // 1. Employee ke khuje ber kora
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found for ID: " + id));

        // 2. Status update kora (ACTIVE / SUSPENDED)
        employee.setStatus(status);

        // 3. Save kora
        employeeRepository.save(employee);

        // (Optional) Jodi Suspend kora hoy, chaile tar User Account disable kore deya jete pare
        // Jate se ar login korte na pare.
    }
}