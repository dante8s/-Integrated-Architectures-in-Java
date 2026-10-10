package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Public coach catalog. Shows only ACTIVE coaches (approved by an admin).
 * Returns CoachView DTOs, never entities.
 */
@Service
@Transactional(readOnly = true)
public class CoachCatalogService {

    // Fixed on the server: a client cannot ask for ?size=100000.
    static final int PAGE_SIZE = 9;

    private final CoachProfileRepository coachProfileRepository;

    public CoachCatalogService(CoachProfileRepository coachProfileRepository) {
        this.coachProfileRepository = coachProfileRepository;
    }

    /**
     * @param page zero-based page number (Spring Data counts pages from 0)
     */
    public Page<CoachView> search(CoachSearchFilter filter, int page) {
        // Empty name -> pattern "%%" matches everybody, so the query needs no special "IS NULL" case.
        String name = filter.getName() == null ? "" : filter.getName().trim().toLowerCase(Locale.ROOT);
        int minExperience = filter.getMinExperience() == null ? 0 : Math.max(filter.getMinExperience(), 0);
        // PageRequest.of(-1, ...) throws an exception -> a hand-edited ?page=-5 just shows the first page.
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);

        return coachProfileRepository
                .search(UserStatus.ACTIVE, filter.getSportId(), "%" + name + "%", minExperience, pageable)
                // Still inside the transaction, so reading the lazy sports here is fine.
                .map(CoachView::from);
    }

    /**
     * Coach page. A PENDING/REJECTED coach looks exactly like a non-existent one (404),
     * so the public site doesn't reveal who is waiting for approval or was rejected.
     */
    public CoachView findActiveCoach(Long id) {
        return coachProfileRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .map(CoachView::from)
                .orElseThrow(() -> new CoachNotFoundException(id));
    }
}
