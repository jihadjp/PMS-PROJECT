package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * [User Service]
 * --------------
 * Ei service class-ta User ba Admin der personal account manage korar jonno.
 * Jemon: Password change kora, Profile picture upload kora, Name/Email update kora.
 * Controller sorasori Repository te hat dey na, ei service er maddhome kaj kore.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Password encrypt korar jonno

    // Admin/User der profile picture ei folder-e save hobe (Project er root folder e create hobe)
    private final String UPLOAD_DIR = "user-photos/";

    // Constructor Injection: Dependency gulo load kora hocche.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // 1. PASSWORD CHANGE LOGIC
    // ==========================================
    /**
     * User jokhon settings theke password change korte chay, tokhon ei method call hoy.
     * - Username: Kar password change hobe?
     * - CurrentPassword: User je purono password dilo.
     * - NewPassword: User je notun password set korte chay.
     */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        // Prothome database theke user ke khuje ber kori
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Check korchi: User je 'Current Password' dilo, seta ki database er password er sathe mile?
            // passwordEncoder.matches() method ta encrypted password er sathe compare kore.
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {

                // Jodi match kore, tahole notun password ta encrypt kore save kore dibo.
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true; // Password changed successfully!
            }
        }
        return false; // Jodi old password vul hoy, tahole false return korbe.
    }

    // ==========================================
    // 2. PROFILE UPDATE LOGIC (With Image Upload)
    // ==========================================
    /**
     * Ei method ta Name, Email abong Profile Picture update kore.
     * 'MultipartFile' holo uploaded image file ta.
     */
    public void updateProfile(String username, String fullName, String email, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Name update korlam
        user.setFullName(fullName);

        // Email update korlam (Jodi notun email dey)
        user.setEmail(email);

        // [Image Upload Logic]
        // Jodi user notun kono chobi select kore thake (File khali na thake)
        if (!file.isEmpty()) {

            // Ekta Unique File Name banacchi (UUID use kore).
            // Karon: Dui jon user jodi same namer (photo.jpg) file upload kore, jate replace na hoye jay.
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Kon path e save hobe ta thik kora
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // Jodi 'user-photos' folder ta na thake, tobe create kore nibe
            if (!Files.exists(Paths.get(UPLOAD_DIR))) {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
            }

            // Asol file ta folder e copy/save kora hocche
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Database e kintu puro chobi save kori na, shudhu path ta save kori.
            // HTML page e ei path dhorei chobi load hobe.
            user.setImageUrl("/user-photos/" + fileName);
        }

        // Sob change database e save kora holo
        userRepository.save(user);
    }

    // ==========================================
    // 3. HELPER METHODS
    // ==========================================

    // Username diye User object khuje anar jonno
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Email diye User object khuje anar jonno (Duplicate email check korar somoy lagbe)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}