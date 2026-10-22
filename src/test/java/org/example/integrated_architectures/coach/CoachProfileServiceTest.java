package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.sport.Sport;
import org.example.integrated_architectures.sport.SportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachProfileServiceTest {

    static final Long COACH_USER_ID = 10L;

    @Mock
    CoachProfileRepository coachProfileRepository;

    @Mock
    SportRepository sportRepository;

    @InjectMocks
    CoachProfileService service;

    // The coach changes the price and the sports: the old sports are replaced, not kept.
    @Test
    void updateChangesPriceAndReplacesSports() {
        CoachProfile profile = new CoachProfile(null, 3, new BigDecimal("20.00"));
        Sport tennis = new Sport("Tennis");
        Sport boxing = new Sport("Boxing");
        profile.getSports().add(tennis);
        when(coachProfileRepository.findByUserId(COACH_USER_ID)).thenReturn(Optional.of(profile));
        when(sportRepository.findAllById(Set.of(2L))).thenReturn(List.of(boxing));

        CoachProfileForm form = new CoachProfileForm();
        form.setExperienceYears(4);
        form.setHourlyPrice(new BigDecimal("30.00"));
        form.setCity("  ");
        form.setSportIds(Set.of(2L));
        service.update(COACH_USER_ID, form);

        assertThat(profile.getHourlyPrice()).isEqualByComparingTo("30.00");
        assertThat(profile.getExperienceYears()).isEqualTo(4);
        assertThat(profile.getCity()).isNull();              // blank -> NULL
        assertThat(profile.getSports()).containsExactly(boxing);
    }
}
