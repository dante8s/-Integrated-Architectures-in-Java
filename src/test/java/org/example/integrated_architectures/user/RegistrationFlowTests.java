package org.example.integrated_architectures.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

// Uses the Postgres from compose.yaml for now; will be replaced by Testcontainers.
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
@AutoConfigureMockMvc
@Transactional
class RegistrationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void studentIsLoggedInRightAfterRegistration() throws Exception {
        mockMvc.perform(post("/register/student").with(csrf())
                        .param("firstName", "Olena")
                        .param("lastName", "Koval")
                        .param("email", "olena@test.local")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(authenticated().withRoles("STUDENT"));
    }
}
