package com.jptechgenius.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * [Password Reset Token Entity]
 * -----------------------------
 * Ei class-ta password reset process er "Secret Key" ba Token manage kore.
 * Jokhon keu "Forgot Password" e click kore, tokhon amra ekta random token banai
 * ebong sheta database-e save kori. Pore user jokhon link-e click kore,
 * amra ei table check kore dekhi token ta valid kina.
 */
@Entity // Database e 'password_reset_token' name table toiri hobe.
@Data // Lombok: Getter, Setter, toString method auto generate korbe.
@NoArgsConstructor // Hibernate er jonno khali constructor lage, tai eta deya.
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    // Ei string tai email-e URL er sathe jabe (Example: abc-123-xyz)
    private String token;

    // Kar jonno ei token ta banano hoyeche?
    // @OneToOne mane ekta token sudhu ekjon user er jonno.
    // fetch = EAGER mane holo, token load korle sathe sathe User er data o load hobe.
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    // Token ta kokhon expire hobe (meyad kobe shesh hobe)
    private LocalDateTime expiryDate;

    // Constructor: Notun token bananor somoy eta call hobe
    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        // Token create howar por theke thik 30 minute porjonto valid thakbe.
        // Tarpor click korle ar kaj hobe na.
        this.expiryDate = LocalDateTime.now().plusMinutes(30);
    }

    // Helper method: Check kora token-er somoy sesh kina
    public boolean isExpired() {
        // Jodi bortoman somoy (now) expiryDate er pore hoy, tar mane expired.
        return LocalDateTime.now().isAfter(expiryDate);
    }
}