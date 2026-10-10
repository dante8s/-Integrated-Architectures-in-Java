package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.sport.Sport;
import org.example.integrated_architectures.sport.SportRepository;
import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserRepository;
import org.example.integrated_architectures.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The catalog logic lives in a JPQL query, so a Mockito unit test wouldn't check anything useful:
 * these tests run the real query against Postgres. Rolled back after each test (@Transactional).
 * All test coaches have the last name "Catalogtest" and every search filters by it,
 * so coaches you registered by hand in the dev database don't break the assertions.
 */
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
@AutoConfigureMockMvc
@Transactional
class CoachCatalogTests {

    private static final String LAST_NAME = "Catalogtest";

    @Autowired
    private CoachCatalogService coachCatalogService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private MockMvc mockMvc;

    private Sport boxing;
    private Sport chess;

    @BeforeEach
    void loadSports() {
        List<Sport> sports = sportRepository.findAllByOrderByNameAsc();   // Boxing, Chess, ... (from V2)
        boxing = sports.get(0);
        chess = sports.get(1);
    }

    @Test
    void onlyActiveCoachesAreListed() {
        saveCoach("Active", UserStatus.ACTIVE, 5, boxing);
        saveCoach("Pending", UserStatus.PENDING, 5, boxing);
        saveCoach("Rejected", UserStatus.REJECTED, 5, boxing);

        assertThat(namesFound(filter(null, null))).containsExactly("Active " + LAST_NAME);
    }

    @Test
    void filtersBySport() {
        saveCoach("Boxer", UserStatus.ACTIVE, 5, boxing);
        saveCoach("Chessplayer", UserStatus.ACTIVE, 5, chess);

        assertThat(namesFound(filter(boxing.getId(), null))).containsExactly("Boxer " + LAST_NAME);
    }

    @Test
    void filtersByMinimalExperience() {
        saveCoach("Junior", UserStatus.ACTIVE, 1, boxing);
        saveCoach("Senior", UserStatus.ACTIVE, 10, boxing);

        assertThat(namesFound(filter(null, 5))).containsExactly("Senior " + LAST_NAME);
    }

    // Guests may open the catalog (SecurityConfig) and the template renders the found coach.
    @Test
    void guestCanOpenCatalog() throws Exception {
        saveCoach("Visible", UserStatus.ACTIVE, 3, boxing);

        mockMvc.perform(get("/coaches").param("name", LAST_NAME))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Visible " + LAST_NAME)));
    }

    @Test
    void guestCanOpenActiveCoachPage() throws Exception {
        CoachProfile profile = saveCoach("Opened", UserStatus.ACTIVE, 3, boxing);

        mockMvc.perform(get("/coaches/{id}", profile.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Opened " + LAST_NAME)));
    }

    // A coach who is not approved must look like a non-existent one.
    @Test
    void pendingCoachPageIsNotFound() throws Exception {
        CoachProfile profile = saveCoach("Hidden", UserStatus.PENDING, 3, boxing);

        mockMvc.perform(get("/coaches/{id}", profile.getId()))
                .andExpect(status().isNotFound());
    }

    private CoachSearchFilter filter(Long sportId, Integer minExperience) {
        CoachSearchFilter filter = new CoachSearchFilter();
        filter.setName(LAST_NAME.toLowerCase());   // lower case on purpose: search ignores case
        filter.setSportId(sportId);
        filter.setMinExperience(minExperience);
        return filter;
    }

    private List<String> namesFound(CoachSearchFilter filter) {
        return coachCatalogService.search(filter, 0).getContent().stream()
                .map(CoachView::fullName)
                .toList();
    }

    private CoachProfile saveCoach(String firstName, UserStatus status, int experienceYears, Sport sport) {
        User user = userRepository.save(new User(firstName.toLowerCase() + "@catalog.test", "hash",
                firstName, LAST_NAME, Role.COACH, status));
        CoachProfile profile = new CoachProfile(user, experienceYears, new BigDecimal("20.00"));
        profile.getSports().add(sport);
        return coachProfileRepository.save(profile);
    }
}
