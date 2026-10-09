package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of the approval rules: the repository is a mock, entities are plain Java objects.
 */
@ExtendWith(MockitoExtension.class)
class CoachApprovalServiceTest {

    @Mock
    CoachProfileRepository coachProfileRepository;

    CoachApprovalService service;

    @BeforeEach
    void setUp() {
        service = new CoachApprovalService(coachProfileRepository);
    }

    @Test
    void approvedCoachBecomesActive() {
        CoachProfile profile = coachWithStatus(UserStatus.PENDING);
        when(coachProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        service.approve(1L);

        assertThat(profile.getUser().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void onlyPendingCoachCanBeApproved() {
        CoachProfile profile = coachWithStatus(UserStatus.REJECTED);
        when(coachProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.approve(1L))
                .isInstanceOf(IllegalStateException.class);
        assertThat(profile.getUser().getStatus()).isEqualTo(UserStatus.REJECTED);
    }

    @Test
    void rejectedCoachGetsStatusAndReason() {
        CoachProfile profile = coachWithStatus(UserStatus.PENDING);
        when(coachProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        service.reject(1L, "  No certificate  ");

        assertThat(profile.getUser().getStatus()).isEqualTo(UserStatus.REJECTED);
        assertThat(profile.getRejectionReason()).isEqualTo("No certificate");
    }

    @Test
    void rejectWithoutReasonFails() {
        // No stubbing: the reason is checked before the repository is called.
        assertThatThrownBy(() -> service.reject(1L, "   "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(coachProfileRepository, never()).findById(any());
    }

    private CoachProfile coachWithStatus(UserStatus status) {
        User user = new User("coach@test.com", "hash", "Ivan", "Petrenko", Role.COACH, status);
        return new CoachProfile(user, 5, new BigDecimal("25.00"));
    }
}
