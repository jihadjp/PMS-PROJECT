package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * [Attendance Repository]
 * -----------------------
 * Ei interface-ta 'attendance' table-er sathe jogajog kore.
 * Employee-ra kobe kobe office-e ashlo, koto somoy kaj korlo, sob data ekhane handle kora hoy.
 * * Note: JpaRepository use koray basic query (save, delete) automatic hoye jay.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * [Fetch Weekly/Monthly Range Data]
     * Ei method-ta duita tarikh er moddhe kar ki obostha, seta ber kore.
     * Example: "1 tarik theke 7 tarik porjonto Rahim saheber hajira dekhan."
     */
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * [Fetch Daily Attendance]
     * 'Attendance Log' page-e jokhon Admin date select kore, tokhon ei method call hoy.
     * Eta oi specific tarikh-er sob employee-r hajira list return kore.
     */
    List<Attendance> findByDate(LocalDate date);

    /**
     * [Prevent Duplicate Entry]
     * Ei method-ta khuboi critical!
     * Jokhon keu "Check In" button chape, age amra check kori—
     * "Ajke ki ei loker nam e kono record already ache?"
     * Jodi thake -> Update kori (Check Out time set kori).
     * Jodi na thake -> Notun Check In record create kori.
     */
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    /**
     * [Active Session Check]
     * Ei method ta Check-Out er somoy lagbe.
     * Amra emon record khujbo jar 'CheckIn' ache kintu 'CheckOut' NULL (mane kaj cholche).
     */
    Optional<Attendance> findByEmployeeIdAndDateAndCheckOutTimeIsNull(Long employeeId, LocalDate date);


    /**
     * [Cleanup Method]
     * Kono Employee delete korle tar sob hajira o delete kore dite hobe.
     * Nahole database garbage hoye jabe.
     */
    void deleteByEmployeeId(Long employeeId);

    List<Attendance> findByStatus(String status);

    // ==========================================
    // CUSTOM QUERY FOR PAYROLL CALCULATION
    // ==========================================

    /**
     * [Fetch Monthly Attendance]
     * Salary generate korar somoy amader jante hoy—
     * "Ei mase (jemon: November 2025) ei employee mot koydin present chilo?"
     * * * Keno @Query?
     * Karon JPA te sorasori 'findByMonth' nai, tai amra SQL er moto kore
     * MONTH() ar YEAR() function use kore nijera query banalam.
     */
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :empId AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    List<Attendance> findByEmployeeIdAndMonth(Long empId, int month, int year);
}