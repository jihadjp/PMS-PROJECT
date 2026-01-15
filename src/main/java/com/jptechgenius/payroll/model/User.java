package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [User Entity]
 * -------------
 * Ei class ta holo System er Login Account maintain korar jonno.
 * Ke Admin, ke Employee, kar password ki - sob ekhane thake.
 * Database e "users" namer table er sathe eta connect kora.
 */
@Entity
@Data // Lombok: Getter, Setter, toString automatic generate korbe.
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    // Username (Login ID)
    // unique = true deya hoyeche jate ek e username dui jon nite na pare.
    @Column(unique = true, nullable = false)
    private String username;

    // --- NEW: Email Field ---
    // Password recovery (Forgot Password) er jonno ei email ta khub joruri.
    // Etao unique hote hobe.
    @Column(unique = true)
    private String email;
    // ------------------------------

    @Column(nullable = false)
    private String password; // Password ta BCrypt diye encrypt kora thakbe (Plain text na).

    // Role: "ADMIN", "SUPER_ADMIN", "EMPLOYEE"
    // SecurityConfig e ei role check korei access deya hoy.
    private String role;

    // --- Profile Details ---
    // Dashboard e "Welcome, Mr. X" dekhanor jonno ei naam ta use hoy.
    private String fullName;

    @Column(name = "image_url")
    private String imageUrl; // Profile picture er file path (e.g., /user-photos/abc.jpg)

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime; // Shesh kobe login korechilo, ta track korar jonno.

    // User ke jodi block/ban korte chai, tahole eta false kore dibo.
    // Delete korar dorkar nai.
    private boolean enabled = true;

    // [Relationship with Employee]
    // Ei User ta asole kon Employee, sheta link kora hoyeche.
    // @OneToOne mane: Ekta Login ID shudhu matro Ekjon Employee-r e hote pare.
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

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
}