package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * [Designation Entity]
 * --------------------
 * Ei class-ta companyr sob Job Title (Podobi) ebong tader Fixed Salary manage korar jonno.
 * Database-e 'designations' name ekta table create hobe.
 * * Keno eta dorkar?
 * Amra chai na je prottek employee add korar somoy manual vabe salary likhte hok.
 * Bhul hote pare (Jemon: Ekjon Engineer 50k, arekjon 45k).
 * Tai amra Designation select korlei automatic salary set hoye jabe.
 */
@Entity
@Data // Lombok annotation: Eita automatic Getter, Setter, toString method banay dibe.
@Table(name = "designations")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID (Auto Increment)

    // Job Title (Example: "Software Engineer", "HR Manager", "Intern")
    // unique = true deya hoyeche jate ek e namer dui ta designation na thake.
    @Column(unique = true, nullable = false)
    private String title;

    // Fixed Salary Amount
    // Jokhon notun employee add kora hobe, ei salary tai auto-fill hobe.
    @Column(nullable = false)
    private Double fixedSalary;
}