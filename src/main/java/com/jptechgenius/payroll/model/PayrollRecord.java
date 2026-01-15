package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID; // UUID import kora hoyeche unique ID er jonno

/**
 * [Payroll Record Entity]
 * -----------------------
 * Ei class-ta holo amader Salary History Table.
 * Jokhon kono masher salary generate kora hoy, tokhon sob calculation (Bonus, Tax, Net Pay)
 * ekhane permanent vabe save hoye thake.
 *----------
 * * Keno separate table?
 * Karon Employee er salary change hote pare. Kintu purono masher salary record jate change na hoy,
 * tai amra prottek masher hisab alada vabe ekhane save kore rakhi (Snapshot).
 */
@Entity
@Data // Lombok: Automatic Getter, Setter, toString generate korbe.
@Table(name = "payroll_records")
public class PayrollRecord {

    // --- SECURITY UPGRADE: UUID ---
    // Ager moto '1, 2, 3' serial ID use na kore amra 'UUID' use korchi.
    // Example: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    // Ete kore URL dekhe keu onno karo payslip guess korte parbe na.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Kon employee er salary eta? Tar ID ta ekhane refer kora ache.
    private Long employeeId;

    // --- SNAPSHOT FIELDS ---
    // Amra Employee table theke sorasori naam dekhaite pari, kintu ekhane alada kore save rakhchi.
    // Karon: Jodi Employee tar naam change kore ba promotion pay,
    // tobuo jeno purono payslip-e oi samoykar naam ar podobi (Designation) thake.
    private String employeeName;
    private String designation;

    // Kon masher salary? (Example: Month: 11, Year: 2025)
    private int month;
    private int year;

    // --- FINANCIAL BREAKDOWN ---

    private Double basicSalary; // Mul beton

    // Overtime + Onnano bonus sob ekhane jog hobe
    private Double bonus;

    // Tax + Late Fine + Penalty sob ekhane jog hobe (Ja salary theke kata hobe)
    private Double deductions;

    // [Final Amount] Employee haate koto taka pabe.
    // Formula: Basic + Bonus - Deductions
    private Double netPay;

    // Payslip e dekhanor jonno employee er chobi o save rakhchi.
    private String imageUrl;

    /**
     * [Image Helper Method]
     * @Transient mane holo: Ei field ta Database-e column hisebe save hobe na.
     * Eta shudhu HTML page e image show korar logic er jonno banano.
     */
    @Transient
    public String getPhotosImagePath() {
        // Jodi user kono chobi upload na kore, tahole ekta default chobi dekhabo.
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "/dist/img/user.jpg";
        }
        // Ar chobi thakle setar path return korbo.
        return imageUrl;
    }

    // Salary ta kobe generate kora hoyeche sei tarikh
    private LocalDate paymentDate;
}