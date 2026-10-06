package org.example.integrated_architectures.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

import java.util.Map;

/**
 * Security for the Thymeleaf pages: HTTP session + form login.
 * A separate stateless JWT chain for /api/** will be added in step 9.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
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
                        .failureHandler(loginFailureHandler())
                        .permitAll())
                // POST /logout (with CSRF token) -> redirect to /login?logout
                .logout(logout -> logout.permitAll());
        // CSRF protection stays enabled: th:action adds the hidden _csrf field to every form.
        return http.build();
    }

    /**
     * Sends the user back to /login with a different parameter for each reason,
     * so login.html can show a specific message.
     */
    private ExceptionMappingAuthenticationFailureHandler loginFailureHandler() {
        var handler = new ExceptionMappingAuthenticationFailureHandler();
        handler.setExceptionMappings(Map.of(
                DisabledException.class.getName(), "/login?pending",
                LockedException.class.getName(), "/login?rejected"));
        handler.setDefaultFailureUrl("/login?error");   // wrong email or password
        return handler;
    }
}
