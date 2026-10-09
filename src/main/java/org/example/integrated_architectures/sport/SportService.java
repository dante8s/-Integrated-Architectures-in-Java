package org.example.integrated_architectures.sport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sports list for registration/catalog and admin CRUD.
 * Read-only transactions by default; methods that change data override it with @Transactional.
 */
@Service
@Transactional(readOnly = true)
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public List<Sport> findAllSorted() {
        return sportRepository.findAllByOrderByNameAsc();
    }

    public Sport findById(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new SportNotFoundException(id));
    }

    @Transactional
    public Sport create(SportForm form) {
        String name = form.getName().trim();
        // Checked here and not only by the UNIQUE constraint: a clear message instead of a DB exception.
        // IgnoreCase: "tennis" and "Tennis" must not both exist.
        if (sportRepository.existsByNameIgnoreCase(name)) {
            throw new SportNameAlreadyUsedException(name);
        }
        Sport sport = new Sport(name);
        sport.setDescription(blankToNull(form.getDescription()));
        return sportRepository.save(sport);
    }

    /**
     * No save() needed: the sport is a managed entity, changes are written on commit (dirty checking).
     */
    @Transactional
    public void update(Long id, SportForm form) {
        Sport sport = findById(id);   // unknown id -> SportNotFoundException (404)
        String name = form.getName().trim();
        // "AndIdNot": the edited sport itself doesn't count, so it can keep its own name.
        if (sportRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new SportNameAlreadyUsedException(name);
        }
        sport.setName(name);
        sport.setDescription(blankToNull(form.getDescription()));
    }

    @Transactional
    public void delete(Long id) {
        Sport sport = findById(id);
        // Without this check the DB would refuse anyway (FK coach_profile_sports.sport_id),
        // but the admin would get an error page instead of a clear message.
        if (sportRepository.isTaughtByAnyCoach(id)) {
            throw new SportInUseException(sport.getName());
        }
        sportRepository.delete(sport);
    }

    // An empty textarea comes as "" - store NULL instead.
    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
