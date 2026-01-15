package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * [Payroll Repository]
 * --------------------
 * Ei interface-ta 'payroll_records' table er sathe communicate kore.
 * Joto dhoroner Salary History, Payslip data ache, sob ekhane handle kora hoy.
 * * * Note: ID er type 'UUID' deya hoise (security r jonno), tai JpaRepository<PayrollRecord, UUID>
 */
@Repository
public interface PayrollRepository extends JpaRepository<PayrollRecord, UUID> {

    // ==========================================
    // 1. DUPLICATE CHECK QUERY
    // ==========================================
    /**
     * [Check Existing Salary]
     * Ei method ta diye check kora hoy je, oi employee er ei mashe
     * salary already generate kora hoise kina.
     * * * Keno List?
     * Karon jodi bhule multiple entry pore jay, tahole list akare ene sob delete kore
     * notun kore fresh salary generate kora hobe (Clean Slate logic).
     * Use: PayrollServiceImpl -> generateMonthlyPayroll()
     */
    List<PayrollRecord> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);

    // ==========================================
    // 2. FILTER QUERY (Salary Sheet)
    // ==========================================
    /**
     * [Filter by Month & Year]
     * Salary Sheet page e jokhon Admin "January 2025" select korbe,
     * tokhon ei method shudhu oi masher record gulo tule anbe.
     */
    List<PayrollRecord> findByMonthAndYear(int month, int year);

    // ==========================================
    // 3. CLEANUP QUERY
    // ==========================================
    /**
     * [Delete History by Employee]
     * Jokhon kono Employee ke delete kora hobe, tokhon tar sob salary history
     * o delete kore dite hobe, nahole database error dibe.
     */
    void deleteByEmployeeId(Long employeeId);
}