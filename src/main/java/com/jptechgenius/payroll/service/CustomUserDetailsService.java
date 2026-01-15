package com.jptechgenius.payroll.service;

import com.jptechgenius.payroll.model.User;
import com.jptechgenius.payroll.repository.UserRepository;
import com.jptechgenius.payroll.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * [Custom User Details Service]
 * -----------------------------
 * Ei class-ta Spring Security er ekta core part.
 * Login korar somoy Spring Security jane na database e user ache kina.
 * Tai amra ei service ta baniyechi jate database theke user khuje ante pare.
 * * @Service annotation ta must, nahole Spring eta khuje pabe na.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor Injection: Repository load kora holo
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * [Load User Logic]
     * User jokhon login form e kichu likhe 'Login' button chape,
     * tokhon Spring Security ei method ta call kore.
     * * 'input' variable e user er deya text (Username ba Email) ashe.
     */
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        // Amra user ke 'Username' othoba 'Email' - je kono ekta diye login korar subidha dicchi.
        // Tai 'findByUsernameOrEmail' method call kora hoyeche.
        // Example: Keu jodi 'admin' likhe, tao kaj korbe. Abar 'admin@gmail.com' likhleo kaj korbe.
        User user = userRepository.findByUsernameOrEmail(input, input)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + input));

        // User pawa gele seta 'CustomUserDetails' object e wrap kore return kori.
        // Spring Security ei object thekei password check kore.
        return new CustomUserDetails(user);
    }
}