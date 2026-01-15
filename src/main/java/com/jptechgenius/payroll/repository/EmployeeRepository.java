package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * [Employee Repository]
 * ---------------------
 * Ei interface-ta amader Java code ar Database er moddhe "Bridge" hisebe kaj kore.
 * Amra JpaRepository extend korechi, tai amader 'save', 'delete', 'findById'
 * er moto common method gulo notun kore likhte hoy na. Spring Boot automatic provide kore.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ==========================================
    // CUSTOM SEARCH METHOD
    // ==========================================

    /**
     * [Search by Name]
     * Ei method-ta ekta "Derived Query Method".
     * Spring Boot eto smart je shudhu method er nam dekhei SQL query banaye fele.
     * * Kajer Dhap:
     * 1. findByName -> Nam diye khujbe.
     * 2. Containing -> Puro nam na, namer je kono onsho milley hobe (LIKE %keyword%).
     * 3. IgnoreCase -> Capital/Small letter matter korbe na (Jemon 'jihad' likhleo 'Jihad' ashbe).
     * * SQL Equivalent: SELECT * FROM employees WHERE LOWER(name) LIKE %keyword%;
     */
    List<Employee> findByNameContainingIgnoreCase(String keyword);
}