package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.Employee;
import java.util.List;

/**
 * [Employee Service Interface]
 * ----------------------------
 * Ei interface-ta amader Employee module er "Contract" ba Rules define kore.
 * Controller kokhono sorasori Repository (Database) call kore na.
 * Controller sob shomoy ei Service er kache help chay.
 * * * Keno interface?
 * Karon future e jodi amra logic change kori, tabu Controller e hat deya lagbe na.
 * Sudhu Implementation class (EmployeeServiceImpl) change korlei hobe.
 */
public interface EmployeeService {

    /**
     * [Get All Employees]
     * Ei method ta database theke shob employee der list ene dibe.
     * Amra Employee Directory page e joto employee dekhi, sob eikhan theke ase.
     */
    List<Employee> getAllEmployees();

    /**
     * [Search Employee]
     * Search bar e jodi keu 'Karim' likhe search dey, tokhon ei method ta kaj korbe.
     * Eta keyword onujayi employee filter kore list dibe.
     */
    List<Employee> searchEmployees(String keyword);

    /**
     * [Save or Update Employee]
     * Ei ekta method diyei 'Notun Employee Add' kora jay, abar 'Old Employee Edit' o kora jay.
     * - Jodi ID na thake -> New Create hobe.
     * - Jodi ID thake -> Old data Update hobe.
     */
    void saveEmployee(Employee employee);

    /**
     * [Get Single Employee]
     * Edit korar somoy form e data fill korar jonno ei method ta lage.
     * ID diye specific ekjon employee er details ber kore ane.
     */
    Employee getEmployeeById(Long id);

    /**
     * [Delete Employee]
     * Database theke employee ke permanently delete kore dibe.
     * * Warning: Sathe sathe tar Attendance, Salary History, Penalty sob delete hoye jabe.
     */
    void deleteEmployeeById(Long id);

    /**
     * [Update Status]
     * Kono employee ke jodi Suspend ba Resign korte hoy, tokhon ei method use hobe.
     * Eta puro record delete na kore shudhu 'Status' ta change kore dey (Example: ACTIVE -> SUSPENDED).
     */
    void updateStatus(Long id, String status);
}