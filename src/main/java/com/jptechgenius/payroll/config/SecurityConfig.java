package com.jptechgenius.payroll.config;

import com.jptechgenius.payroll.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [Security Config Class]
 * -----------------------
 * Ei class ta puro project er security guard er moto kaj kore.
 * Ke kon page e dhukte parbe, kar access ache, login kivabe hobe - sob ekhane control kora hoy.
 */
@Configuration // Spring ke bole je eta ekta configuration class, project start holei eta load hobe.
@EnableWebSecurity // Project e Spring Security enable kore dey.
public class SecurityConfig {

    // Ei service ta database theke user er username/password check kore.
    private final CustomUserDetailsService userDetailsService;

    // Login successful hole user ke dashboard e pathano hobe naki employee portal e,
    // sei logic ta ei handler e ache.
    private final CustomLoginSuccessHandler successHandler;

    // Constructor Injection: Spring automatic dependency gulo load kore dibe.
    public SecurityConfig(CustomUserDetailsService userDetailsService, CustomLoginSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    /**
     * [Main Security Filter Chain]
     * Ekhane amra sob rules set kori. Eita basically ekta filter er moto kaj kore.
     * Request asle age ekhane check hobe, tarpor controller e jabe.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF (Cross-Site Request Forgery) protection on rakhlam default vabe.
                // Eta hacking theke site ke bachay.
                .csrf(Customizer.withDefaults())
                // .csrf(AbstractHttpConfigurer::disable) // Dev mode e thakle majhe majhe off rakha hoy.

                // --- URL AUTHORIZATION RULES ---
                // Ekhane bola hocche kon URL e ke dhukte parbe.
                .authorizeHttpRequests(auth -> auth

                        // 1. Static Resources (CSS, JS, Images) sobar jonno open (Login chara access kora jabe).
                        .requestMatchers("/dist/**", "/plugins/**", "/images/**", "/css/**", "/js/**", "/user-photos/**").permitAll()

                        // 2. Public Pages: Login, Error, Help, Privacy Policy - egula sobar jonno open.
                        .requestMatchers("/login", "/logout", "/error", "/access-denied", "/help","/home", "/privacy-policy", "/terms-of-use", "/forgot-password", "/reset-password").permitAll()

                        // 3. Payslip Access: Payslip employee nijero dekhte pare abar admin o pare. Tai duijonkei permission dilam.
                        .requestMatchers("/payroll/payslip/**").hasAnyRole("EMPLOYEE", "ADMIN", "SUPER_ADMIN")

                        // 4. Employee Portal: Employee der nijer dashboard e sudhu tara ebong admin dhukte parbe.
                        .requestMatchers("/employee-portal/**").hasAnyRole("EMPLOYEE", "ADMIN", "SUPER_ADMIN")

                        // 5. Admin Access: Baki joto page ache (Add Employee, Salary Generate etc),
                        // segula sudhu 'ADMIN' ba 'SUPER_ADMIN' access korte parbe.
                        .anyRequest().hasAnyRole("ADMIN", "SUPER_ADMIN")
                )

                // --- LOGIN CONFIGURATION ---
                .formLogin(form -> form
                        .loginPage("/login") // Amader custom banano login page ta use hobe.
                        .successHandler(successHandler) // Login hole kon page e jabe, sei logic ta ekhane set kora holo.
                        .permitAll() // Login page ta sobar jonno open.
                )

                // --- LOGOUT CONFIGURATION ---
                .logout(logout -> logout
                        .logoutUrl("/logout") // Logout button click korle ei URL hit hobe.
                        .logoutSuccessUrl("/login?logout") // Logout howar por abar login page e niye jabe.
                        // .deleteCookies("JSESSIONID") // Session cookie delete kore dibe (Optional).
                        .permitAll()
                )

                // --- ERROR HANDLING ---
                // Jodi keu emon page e jete chay jar permission tar nai,
                // tahole take 'access-denied' page e pathiye dibe.
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build(); // Sob config eksathe kore security chain ta build korlo.
    }

    // --- AUTHENTICATION HELPERS ---

    /**
     * Password Encoder:
     * Database e password plain text (jemon: "1234") hisebe rakha unsafe.
     * Tai amra 'BCrypt' use kori jate password encrypted thake.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider:
     * Eita login er somoy kaj kore. User jei password dey ar database e ja ache,
     * duti match kore dekhe thik ache kina.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // User khuje anar service set kora holo.
        provider.setPasswordEncoder(passwordEncoder()); // Password melanor jonno encoder set kora holo.
        return provider;
    }
}