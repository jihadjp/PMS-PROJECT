package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.PayrollRecord;
import java.util.List;
import java.util.UUID; // Security r jonno UUID import kora hoise

/**
 * [Payroll Service Interface]
 * ---------------------------
 * Ei interface-ta holo 'Payroll' ba 'Beton' system er contract.
 * Salary generate kora, history dekha, ebong payslip print korar
 * sob logic ekhane define kora thake. Implementation class e egula code kora hobe.
 */
public interface PayrollService {

    /**
     * [Generate Salary Engine]
     * Ei method ta call korle oi masher sob employee der salary automatic calculate hobe.
     * Hajira, penalty, tax sob hisab kore net pay ber korbe ebong database e save korbe.
     */
    void generateMonthlyPayroll(int month, int year);

    /**
     * [Get All History]
     * Companyr shuru theke aj porjonto joto salary deya hoyeche, tar sob record anar jonno.
     * Eta reports ba audit er jonno lage.
     */
    List<PayrollRecord> getAllRecords();

    /**
     * [Get Single Payslip]
     * Ekjon employee er nirdisto ekta masher payslip dekhar jonno ei method.
     * * Security Fix:
     * Age amra 'Long ID' (1, 2, 3) use kortam, jeta guess kora sohoj chilo.
     * Ekhon amra 'UUID' (e.g. a0eebc99-...) use korchi jate keu URL hack kore onner payslip na dekhte pare.
     */
    PayrollRecord getRecordById(UUID id); // Long -> UUID change kora hoyeche

    /**
     * [Filter Salary Sheet]
     * Salary Sheet page e jokhon Admin mash ar bochor select kore filter chapbe,
     * tokhon ei method ta kaj korbe. Eta shudhu oi nirdisto masher record gulo dekhabe.
     */
    List<PayrollRecord> getRecordsByMonthAndYear(int month, int year);
}