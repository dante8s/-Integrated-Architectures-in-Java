package org.example.integrated_architectures.sport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of the sport rules: the repository is a mock.
 */
@ExtendWith(MockitoExtension.class)
class SportServiceTest {

    @Mock
    SportRepository sportRepository;

    SportService service;

    @BeforeEach
    void setUp() {
        service = new SportService(sportRepository);
    }

    @Test
    void duplicateNameIsRejected() {
        // " tennis " is trimmed before the check
        when(sportRepository.existsByNameIgnoreCase("tennis")).thenReturn(true);

        assertThatThrownBy(() -> service.create(form(" tennis ")))
                .isInstanceOf(SportNameAlreadyUsedException.class);
        verify(sportRepository, never()).save(any());
    }

    @Test
    void sportTaughtByCoachCannotBeDeleted() {
        when(sportRepository.findById(1L)).thenReturn(Optional.of(new Sport("Tennis")));
        when(sportRepository.isTaughtByAnyCoach(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(SportInUseException.class);
        verify(sportRepository, never()).delete(any());
    }

    // Editing the description only: the sport must not "collide" with its own name.
    @Test
    void editedSportCanKeepItsOwnName() {
        Sport sport = new Sport("Tennis");
        when(sportRepository.findById(1L)).thenReturn(Optional.of(sport));
        when(sportRepository.existsByNameIgnoreCaseAndIdNot("Tennis", 1L)).thenReturn(false);

        SportForm form = form("Tennis");
        form.setDescription("Racket sport");
        service.update(1L, form);

        assertThat(sport.getName()).isEqualTo("Tennis");
        assertThat(sport.getDescription()).isEqualTo("Racket sport");
    }

    private static SportForm form(String name) {
        SportForm form = new SportForm();
        form.setName(name);
        return form;
    }
}
