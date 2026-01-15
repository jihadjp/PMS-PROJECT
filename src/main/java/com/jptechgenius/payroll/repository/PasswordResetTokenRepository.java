package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.PasswordResetToken;
import com.jptechgenius.payroll.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * [Password Reset Token Repository]
 * ---------------------------------
 * Ei interface-ta Database-er 'password_reset_token' table-er sathe communicate kore.
 * Token save kora, khuje ber kora, delete kora - ei dhoroner kaj ekhane hoy.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // ==========================================
    // 1. FIND TOKEN (Verification)
    // ==========================================
    /**
     * [Find by Token String]
     * User jokhon email-er link-e click korbe, tokhon URL theke token-ta pabo.
     * Sei token ta asole valid kina, seta check korar jonno ei method ta use hoy.
     */
    Optional<PasswordResetToken> findByToken(String token);

    // ==========================================
    // 2. DUPLICATE CHECK (Critical Fix)
    // ==========================================
    /**
     * [Find by User Object]
     * Ei method ta khuboi important.
     * User jokhon "Forgot Password" e click kore, age amra check kori tar name
     * already kono token create kora ache kina.
     * - Jodi thake -> Oita update kori.
     * - Jodi na thake -> Notun create kori.
     * * * Keno dorkar?
     * Amader database-e 'user_id' unique kora ache. Tai check na kore sorasori
     * insert korte gele 'Duplicate Entry' error asto.
     */
    Optional<PasswordResetToken> findByUser(User user);

    // ==========================================
    // 3. CLEANUP
    // ==========================================
    /**
     * [Delete by User ID]
     * Kono user-er purono sob token delete korar jonno eta use kora hoy.
     * Jemon: Password change hoye gele purono token rekhe labh nai, tai delete kore dei.
     */
    void deleteByUserId(Long userId);
}