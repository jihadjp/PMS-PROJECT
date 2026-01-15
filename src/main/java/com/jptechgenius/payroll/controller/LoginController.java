package com.jptechgenius.payroll.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * [Login Controller]
 * ------------------
 * Ei controller-tar main kaj holo Login page ebong Access Denied (permission error) page show kora.
 * Jokhon keu login korte chay ba emon kothao jete chay jekhane tar permission nai, tokhon ei controller kaj kore.
 */
@Controller // Spring ke bolchi je eta ekta Web Controller, mane eita HTML page return korbe.
public class LoginController {

    /**
     * [Show Login Form]
     * Jokhon keu browser-e '/login' hit korbe, tokhon ei method ta call hobe.
     * Eita sojasuji 'login.html' page ta user ke dekhabe.
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // templates/login.html load hobe
    }

    /**
     * [Access Denied Page]
     * Jodi kono user emon page-e dhukte chay jar permission tar nai
     * (Jemon: Employee hoye Admin page e jete chawa), tokhon Spring Security take ekhane pathiye dibe.
     * Amra tokhon 'access-denied.html' page ta dekhabo, jekhane lekha thakbe "403 - Not Authorized".
     */
    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied"; // templates/access-denied.html load hobe
    }
}