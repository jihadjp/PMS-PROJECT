package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.PasswordResetToken;
import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.repository.PasswordResetTokenRepository;
import com.jptechgenius.payroll.repository.UserRepository;
import com.jptechgenius.payroll.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * [Forgot Password Controller]
 * ----------------------------
 * Ei controller-ta password reset er puro process handle kore.
 * 1. User email dibe -> Token generate hobe -> Email jabe.
 * 2. User link e click korbe -> Token verify hobe -> New password set hobe.
 */
@Controller
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection: Sob dependency autowire kora holo.
    public ForgotPasswordController(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // STEP 1: REQUEST PASSWORD RESET
    // ==========================================

    /**
     * [Show Forgot Password Page]
     * User jokhon 'Forgot Password?' link e click korbe, tokhon ei page ta ashbe.
     * Ekhane user tar email address dibe.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * [Process Request & Send Email]
     * User email submit korle ei method call hobe.
     * 1. Check kora hobe email valid kina.
     * 2. Token generate kora hobe.
     * 3. Email pathano hobe.
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(HttpServletRequest request,
                                        @RequestParam("email") String userEmail,
                                        Model model) {

        // Database e check korchi user ache kina
        Optional<User> userOptional = userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            // Jodi user na thake, error message dibo
            model.addAttribute("error", "We could not find an account with that email.");
            return "forgot-password";
        }

        User user = userOptional.get();
        // Ekta random unique token generate korlam (e.g. abc-123-xyz)
        String token = UUID.randomUUID().toString();

        // --- Token Management Logic ---
        // Check korchi ei user er age kono token chilo kina.
        // Thakle oitai update korbo (Duplicate key error thekanor jonno).
        // Na thakle notun create korbo.
        PasswordResetToken myToken = tokenRepository.findByUser(user)
                .orElse(new PasswordResetToken());

        myToken.setToken(token);
        myToken.setUser(user);
        // Token ta 30 minute por expire hoye jabe
        myToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(myToken); // Database e token save

        // Reset Link Create: http://localhost:9090/reset-password
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // Email send korchi link shoho
        emailService.sendResetTokenEmail(user.getEmail(), token, appUrl + "/reset-password");

        // Success hole Confirmation page e pathiye dibo
        model.addAttribute("email", userEmail);
        return "forgot-password-confirmation";
    }

    // ==========================================
    // STEP 2: SET NEW PASSWORD
    // ==========================================

    /**
     * [Show Reset Form]
     * User jokhon email er link e click korbe, tokhon ei page ashbe.
     * Ekhane amra check kori token ta valid kina ba expire hoye geche kina.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Token ta database e khuje dekhi
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        // Jodi token na thake ba expire hoye jay, tobe error dibo
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "Invalid or expired token.");
            return "login";
        }

        // Token valid hole model e add kori, jate form submit korle abar pawa jay
        model.addAttribute("token", token);
        return "reset-password"; // New password deyar form open hobe
    }

    /**
     * [Save New Password]
     * User notun password diye submit korle ei method kaj korbe.
     * Password update hobe ebong token delete kora hobe.
     */
    @PostMapping("/reset-password")
    @Transactional // Transactional mane puro kaj ekbare hobe, nahole rollback hobe
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {

        // Abaro check kori token valid kina (Securityr jonno)
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "Token invalid or expired.");
            return "reset-password";
        }

        // Token theke user ke ber kori
        User user = tokenOpt.get().getUser();
        // Notun password ta encrypt kore save kori
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Token ta use kora shesh, tai delete kore dilam jate r use na kora jay
        tokenRepository.delete(tokenOpt.get());

        // Login page e pathiye dilam success message shoho
        return "redirect:/login?resetSuccess";
    }
}