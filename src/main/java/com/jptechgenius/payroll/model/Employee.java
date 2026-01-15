package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * [Employee Entity]
 * -----------------
 * Ei class-ta amader Database er 'employees' table er sathe sorasori connected.
 * Ekhane amra "Validation Annotations" use korechi jate keu vul data (jemon fake number, salary 0) dite na pare.
 */
@Entity
@Data // Lombok: Getter, Setter, toString automatic generate korbe.
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    // --- NAME VALIDATION ---
    // 1. Naam khali rakha jabe na.
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.]+$", message = "Name can only contain letters, spaces, and dots")
    private String name;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String department;

    // --- PHONE VALIDATION ---
    @NotBlank(message = "Phone number is required")
    // 3. Regex Regex diye check korchi: A-Z, Spaces (\s) er sathe Dot (.) allow kora holo.
    // Kintu number ba onno special symbol (@, #) allow hobe na.
    @Pattern(regexp = "^01[3-9]\\d{8}$", message = "Invalid BD number! Must be 11 digits starting with 01")
    @Column(unique = true)
    private String phoneNumber;

    // --- EMAIL VALIDATION ---
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format (e.g., user@example.com)")
    private String email;

    private String address;
    private String bankName;
    private String bankAccountNo;

    @NotNull(message = "Joining date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    // --- SALARY & OVERTIME VALIDATION ---

    @NotNull(message = "Basic salary is required")
    @Min(value = 0, message = "Salary cannot be negative") // Taka kokhono negative hoy na
    private Double basicSalary;

    // [NEW ADDITION] Dynamic Overtime Calculation er jonno
    @NotNull(message = "Overtime rate is required")
    @Min(value = 0, message = "Rate cannot be negative")
    private Double overtimeRatePerHour = 0.0; // Default 0.0 rakha holo safety r jonno

    // Fixed deduction (optional)
    @Min(value = 0, message = "Deduction cannot be negative")
    private Double deductions = 0.0;

    // Default status 'ACTIVE'
    private String status = "ACTIVE";

    private String imageUrl; // Database e shudhu chobir link/path ta save hobe.

    /**
     * [Helper Method for Image]
     * @Transient mane holo ei field-ta database e column hisebe toiri hobe na.
     */
    @Transient
    public String getPhotosImagePath() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "/dist/img/user.jpg";
        }
        return imageUrl;
    }
}