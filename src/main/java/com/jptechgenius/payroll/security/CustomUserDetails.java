package com.jptechgenius.payroll.security;

import com.jptechgenius.payroll.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * [Custom User Details - The Adapter]
 * -----------------------------------
 * Spring Security amader nijer banano 'User' class ta chene na.
 * O sudhu 'UserDetails' interface ta bujhte pare.
 * Tai amra ei class ta baniyechi jeta amader database 'User' ke
 * Spring Security-r bojhar moto kore translate kore dey.
 * Eita ekta "Bridge" ba "Adapter" er moto kaj kore.
 *
 * @param user -- GETTER --
 *             [Get Raw User Object]
 *             Ei method ta khuboi important.
 *             Amra HTML page-e (Thymeleaf) jokhon user er chobi, full name,
 *             ba onno kono custom data dekhaite chai, tokhon ei method ta call kori.
 *             Use in HTML: $
 *             {
 *             #authentication.principal.user.fullName
 *             }
 *             Database theke ana asol User object ta ekhane rakhchi.
 */
public record CustomUserDetails(User user) implements UserDetails {

    // Constructor: Jokhon ei class ta toiri hobe, tokhon e user data pass kore dibo.

    /**
     * [Role Management]
     * Spring Security role check korar somoy "ROLE_" prefix khoje.
     * Kintu amader database e "ADMIN" lekha ache, "ROLE_ADMIN" na.
     * Tai ekhane amra manual vabe "ROLE_" jog kore dicchi jate security config thikmoto kaj kore.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    // Password ta Spring ke dicchi jate o check korte pare
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Username ta Spring ke dicchi
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // ==========================================
    // ACCOUNT STATUS FLAGS (Default Settings)
    // ==========================================
    // Nicher method gulo diye account lock/expire kora jay.
    // Ekhonkar moto amra sob true (valid) return korchi.

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account kokhono expire hobe na.
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account kokhono lock hobe na.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password er meyed sesh hobe na.
    }

    /**
     * [Account Enable/Disable]
     * Eita amader database er 'enabled' column er sathe connect kora.
     * Jodi database e enabled = false kore dei, tahole user ar login korte parbe na.
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}