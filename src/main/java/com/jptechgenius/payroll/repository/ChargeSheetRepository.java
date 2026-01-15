package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.ChargeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * [ChargeSheet Repository]
 * ------------------------
 * Ei interface-ta 'charge_sheets' table-er sathe jogajog kore.
 * Employee-der fine, penalty amount, ebong status (PENDING/DEDUCTED)
 * eikhan thekei manage kora hoy.
 */
@Repository
public interface ChargeSheetRepository extends JpaRepository<ChargeSheet, Long> {

    /**
     * [Find Fines by Status]
     * Ei method ta diye amra check kori kono employee-er 'PENDING' fine ache kina.
     * Jemon: Employee Portal e employee nijer pending fine dekhte chay,
     * othoba admin dekhte chay kar kar taka kata baki ache.
     */
    List<ChargeSheet> findByEmployeeIdAndStatus(Long employeeId, String status);

    // ==========================================
    // CRITICAL PAYROLL QUERY
    // ==========================================
    /**
     * [Find Monthly Fines for Payroll]
     * Payroll Service jokhon salary calculate kore, tokhon ei method ta call kore.
     * * Kaj: "Ei masher 1 tarik theke 30 tarik porjonto, ei employee mot koyta fine kheyeche?"
     * * Keno 'Between' Date?
     * Karon salary generate korar somoy amader jante hobe shudhu 'OI MASHER' fine gulo,
     * ager ba porer masher gulo na.
     */
    List<ChargeSheet> findByEmployeeIdAndIssueDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * [Cleanup Method]
     * Jodi kono employee ke database theke delete kora hoy,
     * tokhon tar sob fine record-o delete kore dite hobe, nahole error dibe.
     */
    void deleteByEmployeeId(Long employeeId);
}