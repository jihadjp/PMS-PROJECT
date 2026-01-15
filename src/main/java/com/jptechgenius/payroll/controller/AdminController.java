package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.security.CustomUserDetails;
import com.jptechgenius.payroll.service.EmployeeService;
import com.jptechgenius.payroll.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [Admin Controller]
 * -------------------
 * Ei controller ta shudhu Admins der kaj-kormo handle kore.
 * Jemon: Profile update kora, Password change kora, Employee ke Suspend/Activate kora.
 * Ekhane '/admin' prefix use kora hoyeche, tai sob URL '/admin/...' diye shuru hobe.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final EmployeeService employeeService;

    // Constructor Injection (SpringBoot automatically service gula inject kore dibe)
    public AdminController(UserService userService, EmployeeService employeeService) {
        this.userService = userService;
        this.employeeService = employeeService;
    }

    // ==========================================
    // 1. PROFILE & SETTINGS (নিজের প্রোফাইল ঠিক করা)
    // ==========================================

    /**
     * [Show Profile Page]
     * Ei method ta Admin er profile page show kore.
     * Login thaka user er username diye database theke user info niye ase.
     */
    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal) {
        String username = principal.getName(); // Current logged-in user er username
        User user = userService.getUserByUsername(username); // Database theke user anlam
        model.addAttribute("user", user); // HTML e user object pathalam
        return "admin/profile"; // admin/profile.html page ta load hobe
    }

    /**
     * [Update Profile]
     * Ekhane Admin tar naam, email, ar chobi update korte parbe.
     * Tobe ekta check ache: Email jeno duplicate na hoy (mane onno karo email jeno use na kore).
     */
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("image") MultipartFile image,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();

            // 1. Current logged-in user ke database theke anlam (ID check korar jonno)
            User currentUser = userService.getUserByUsername(username);

            // 2. Email Duplicate Check Logic
            // Jodi database e ei email diye onno kono user thake, tahole error dibo.
            User existingUserByEmail = userService.getUserByEmail(email);

            // Jodi user pawa jay EBONG sei user ami na hoy (ID different), tobe error.
            if (existingUserByEmail != null && !existingUserByEmail.getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "This email is already registered by another user!");
                return "redirect:/admin/profile";
            }

            // 3. Database Update: Naam, Email, Image save kora
            userService.updateProfile(username, fullName, email, image);

            // 4. Session Update (Auto Refresh)
            // Database update holeo browser e purono naam dekhte pare, tai Session update kora lagbe.
            // Eita korle Logout charai sathe sathe naam/chobi change hoye jabe.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

            userDetails.user().setFullName(fullName);
            userDetails.user().setEmail(email);

            // Jodi notun chobi deya hoy, tobe session eo image URL update kora holo
            if (!image.isEmpty()) {
                User updatedUser = userService.getUserByUsername(username);
                userDetails.user().setImageUrl(updatedUser.getImageUrl());
            }

            // Success message dekhabo
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (IOException e) {
            // Jodi chobi upload e kono problem hoy
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, e);
            redirectAttributes.addFlashAttribute("error", "Error uploading image. Please try again.");
        } catch (Exception e) {
            // Onno jekono error hole
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, null, e);
            redirectAttributes.addFlashAttribute("error", "Something went wrong!");
        }

        return "redirect:/admin/profile";
    }

    // Settings page show korar jonno
    @GetMapping("/settings")
    public String showSettingsPage() {
        return "admin/settings";
    }

    /**
     * [Change Password]
     * User tar old password change kore notun password set korbe.
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        // Service layer e check hobe old password thik ache kina
        boolean isChanged = userService.changePassword(username, currentPassword, newPassword);

        if (isChanged) {
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
        }
        return "redirect:/admin/settings";
    }

    // ==========================================
    // 2. EMPLOYEE MANAGEMENT ACTIONS
    // ==========================================

    /**
     * [Activate Employee]
     * Jodi kono employee 'SUSPENDED' thake, take abar 'ACTIVE' korar jonno.
     * Note: Frontend theke POST request aste hobe, GET request dile kaj korbe na (Security).
     */
    @PostMapping("/activate/{id}")
    public String activateEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            // Employee er status change kore 'ACTIVE' kora holo
            employeeService.updateStatus(id, "ACTIVE");
            redirectAttributes.addFlashAttribute("success", "Employee activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error activating employee.");
        }

        // Kaj sesh hole employee list page e ferot jabe
        return "redirect:/employees";
    }

    /**
     * [Suspend Employee]
     * Kono employee ke samoyik vabe kaj theke biroto rakhar jonno.
     * Eita korle se login korte parbe na.
     */
    @PostMapping("/suspend/{id}")
    public String suspendEmployee(@PathVariable Long id,
                                  @RequestParam(required = false) String reason, // Reason optional rakha hoyeche
                                  RedirectAttributes redirectAttributes) {

        try {
            // Status change kore 'SUSPENDED' kora holo
            employeeService.updateStatus(id, "SUSPENDED");

            // Future Plan: Jodi chai, suspension er karon (Reason) database e save kora jabe.
            // ekhon eta comment kora ache.

            redirectAttributes.addFlashAttribute("success", "Employee suspended successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error suspending employee.");
        }

        return "redirect:/employees";
    }

    /**
     * [Delete Employee]
     * Ekjon employee ke permanently delete korar jonno.
     * Note: Age check kora hobe tar kono Attendance/Salary record ache kina.
     * Segula age delete korte hobe (Cascading), nahole database error dibe.
     * Ei puro logic ta 'EmployeeServiceImpl' e kora ache.
     */
    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            // Service layer ke bolchi delete korte. Oikhane sob cleanup logic ache.
            employeeService.deleteEmployeeById(id);
            redirectAttributes.addFlashAttribute("success", "Employee ID " + id + " deleted successfully!");
        } catch (Exception e) {
            // Jodi kono karone delete na hoy (Foreign Key issue ba onno kichu)
            redirectAttributes.addFlashAttribute("error", "Error deleting employee. Please try again.");
        }

        return "redirect:/employees";
    }
}