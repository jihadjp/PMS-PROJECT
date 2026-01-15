package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * [Attendance Entity]
 * -------------------
 * Ei class-ta database-er 'attendance' table-er sathe map kora.
 * Protidin kar hajira, time tracking, ar overtime-er hisab ekhane thakbe.
 */
@Entity
@Data // Lombok: Getter, Setter, toString automatic banabe, amader likhte hobe na.
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Database er Primary Key (Auto Increment)

    @Column(nullable = false)
    private Long employeeId; // Kar attendance? Tar ID ta ekhane rakhbo.

    @Column(nullable = false)
    private LocalDate date; // Kon tarikh-er hajira? (Example: 2025-12-05)

    // ==========================================
    // PROFESSIONAL TIME TRACKING FIELDS
    // ==========================================

    // 1. Check-In Time
    // Employee kokhon office e dhuklo (Punch In time).
    // Eta null thaka mane se ashe nai.
    private LocalTime checkInTime;

    // 2. Check-Out Time
    // Employee kokhon office theke ber holo (Punch Out time).
    // Eta null thakle bujhbo se ekhono office e ache (kaj korche).
    private LocalTime checkOutTime;

    @Column(columnDefinition = "TEXT")
    private String disputeReason; // Field for saving dispute explanation

    // 3. Total Work Duration
    // Saradine mot koto ghonta kaj korlo (CheckOut - CheckIn).
    // Eta payroll calculate korar somoy lagbe.
    private Double workHours = 0.0;

    // 4. Current Status
    // Ekhon tar obostha ki?
    // Values hote pare: "CHECKED_IN" (Kaj korche), "CHECKED_OUT" (Chuti), "ABSENT" (Ase nai).
    @Column(length = 20)
    private String status;

    // ==========================================
    // SUPPORTING FIELDS
    // ==========================================

    // Se ki adou present chilo?
    // Check-in korle eta automatic 'true' hoye jabe.
    private boolean isPresent = false;

    // Overtime Calculation
    // Jodi standard time (dhoren 9 ghonta) er beshi kaj kore, baki time-ta ekhane save hobe.
    // Eita use kore bonus salary hisab hobe.
    private Double overtimeHours = 0.0;
}