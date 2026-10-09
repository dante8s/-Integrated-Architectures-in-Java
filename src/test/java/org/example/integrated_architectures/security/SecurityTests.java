package org.example.integrated_architectures.security;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserRepository;
import org.example.integrated_architectures.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application context + MockMvc: requests go through the real SecurityFilterChain.
 * MockMvc runs in the test thread, so @Transactional rolls back the users created here.
 */
// Uses the Postgres from compose.yaml for now; will be replaced by Testcontainers.
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
@AutoConfigureMockMvc
@Transactional
class SecurityTests {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentGetsForbiddenOnAdminPages() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    void activeStudentCanLogIn() throws Exception {
        saveUser("student@test.local", Role.STUDENT, UserStatus.ACTIVE);

        mockMvc.perform(formLogin("/login").user("email", "student@test.local").password(PASSWORD))
                .andExpect(authenticated().withRoles("STUDENT"));
    }

    @Test
    void pendingCoachCannotLogIn() throws Exception {
        saveUser("coach@test.local", Role.COACH, UserStatus.PENDING);

        mockMvc.perform(formLogin("/login").user("email", "coach@test.local").password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?pending"));
    }

    // Status is checked only after a correct password (SecurityConfig.authenticationProvider),
    // so a wrong password never reveals that the account exists and is pending/rejected.
    @Test
    void wrongPasswordDoesNotRevealCoachStatus() throws Exception {
        saveUser("coach@test.local", Role.COACH, UserStatus.PENDING);

        mockMvc.perform(formLogin("/login").user("email", "coach@test.local").password("wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void rejectedCoachSeesReasonAfterLogin() throws Exception {
        User coach = saveUser("rejected@test.local", Role.COACH, UserStatus.REJECTED);
        CoachProfile profile = new CoachProfile(coach, 3, new BigDecimal("20.00"));
        profile.setRejectionReason("No certificate");
        coachProfileRepository.save(profile);

        MvcResult result = mockMvc.perform(formLogin("/login").user("email", "rejected@test.local").password(PASSWORD))
                .andExpect(redirectedUrl("/login?rejected"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        mockMvc.perform(get("/login").param("rejected", "").session(session))
                .andExpect(content().string(containsString("No certificate")));
    }

    private User saveUser(String email, Role role, UserStatus status) {
        return userRepository.save(new User(email, passwordEncoder.encode(PASSWORD), "Test", "User", role, status));
    }
}
