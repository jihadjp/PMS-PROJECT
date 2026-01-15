package com.jptechgenius.payroll.security;

import com.jptechgenius.payroll.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * [Authentication Success Listener]
 * ---------------------------------
 * Ei class-ta ekta "Spy" er moto kaj kore.
 * Jokhon kono user successful vabe login kore, Spring Security ekta 'Event' fire kore.
 * Ei class sei event ta shune (Listen) user er 'last_login_time' ta update kore dey.
 * Ete kore amra jante pari ke kokhon sesh login korechilo.
 */
@Component // Spring ke bolchi: "Eta ekta Bean, start holei load koro."
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserRepository userRepository;

    // Constructor Injection: Repository load korlam database e save korar jonno.
    public AuthenticationSuccessListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * [Event Trigger Logic]
     * Ei method ta automatic call hobe jokhon keu login korbe.
     */
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        // Login kora user er details ta ber kore nilam
        Object principal = event.getAuthentication().getPrincipal();

        // Check korchi asolei user ki valid UserDetails object kina
        if (principal instanceof UserDetails) {

            // Username ta ber korlam
            String username = ((UserDetails) principal).getUsername();

            // Database e user khuje ber korlam
            userRepository.findByUsername(username).ifPresent(user -> {

                // [MAIN LOGIC]
                // User er 'lastLoginTime' field ta update kore ekhonkar somoy dilam.
                user.setLastLoginTime(LocalDateTime.now());

                // Database e save kore dilam.
                userRepository.save(user);
            });
        }
    }
}