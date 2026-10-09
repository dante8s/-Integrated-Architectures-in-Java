package org.example.integrated_architectures.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security for the Thymeleaf pages: HTTP session + form login.
 * A separate stateless JWT chain for /api/** will be added in step 9.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                     LoginFailureHandler loginFailureHandler) throws Exception {
        http
                // Rules are checked top to bottom, the first matching one wins.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register/**", "/login", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Must be the last rule: everything not listed above needs a logged-in user.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")              // our own page instead of the generated one
                        .usernameParameter("email")       // name of the input in login.html
                        .failureHandler(loginFailureHandler)
                        .permitAll())
                // POST /logout (with CSRF token) -> redirect to /login?logout
                .logout(logout -> logout.permitAll());
        // CSRF protection stays enabled: th:action adds the hidden _csrf field to every form.
        return http.build();
    }

    /**
     * Checks email + password with our AppUserDetailsService and BCrypt.
     * By default Spring checks the account status (enabled/locked) BEFORE the password,
     * so anyone who knows a coach's email would see "pending"/"rejected" (and now the reason).
     * Here the status is checked only AFTER a correct password.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(AppUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setPreAuthenticationChecks(user -> { });                           // nothing before the password
        provider.setPostAuthenticationChecks(new AccountStatusUserDetailsChecker()); // enabled, locked, expired
        return provider;
    }
}
