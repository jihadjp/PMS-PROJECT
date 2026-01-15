package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * [ChargeSheet Entity]
 * --------------------
 * Ei class-ta Employee der 'Penalty' ba 'Fine' er hisab rakher jonno.
 * Database-e 'charge_sheets' name ekta table create hobe.
 * Jokhon kono employee kono rule break korbe (jemon late asha),
 * tokhon tar name ekta ChargeSheet issue kora hobe.
 */
@Entity
@Data // Lombok annotation: Eita automatic Getter, Setter, toString method banay dibe.
@Table(name = "charge_sheets")
public class ChargeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID for this specific fine record

    // Kar name fine kora hocche?
    // @ManyToOne mane: Ekjon Employee er onek gula Fine/ChargeSheet thakte pare.
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Keno fine kora holo? (Example: "Late Attendance", "Misbehavior", "Lost Office Asset")
    @Column(nullable = false)
    private String reason;

    // Koto taka fine kora hoyeche?
    // Ei amount tai masher sheshe Salary generate korar somoy 'Deductions' hisebe kata jabe.
    @Column(nullable = false)
    private Double penaltyAmount;

    // Kobe fine ta issue kora hoyeche (Sadharonoto ajker tarikh).
    private LocalDate issueDate;

    // [CRITICAL LOGIC]
    // Status 'PENDING' thaka mane ei taka ekhono salary theke kata hoy nai.
    // Payroll Service salary bananor somoy check korbe: Jodi status 'PENDING' hoy, tobei taka katbe.
    // Taka kete neyar por status automatic 'DEDUCTED' kore deya hobe, jate porer mase abar na kate.
    private String status = "PENDING";
}