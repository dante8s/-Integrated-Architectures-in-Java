package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {

    Optional<CoachProfile> findByUserId(Long userId);

    // WHERE user.email = ? (the user is joined automatically by the derived query)
    Optional<CoachProfile> findByUserEmail(String email);

    /**
     * Derived query on a nested property: WHERE user.status = ? ORDER BY user.createdAt.
     * @EntityGraph loads user and sports in the same SELECT (JOIN FETCH),
     * because with open-in-view=false the template cannot load lazy fields later.
     */
    @EntityGraph(attributePaths = {"user", "sports"})
    List<CoachProfile> findByUserStatusOrderByUserCreatedAtAsc(UserStatus status);

    /**
     * Coach catalog: one page of coaches with the given status, filtered.
     * - JOIN FETCH user: user is a to-one relation, so fetching it doesn't break LIMIT/OFFSET.
     *   Sports (to-many) are NOT fetched here (Hibernate would paginate in memory) -
     *   they are loaded with one extra query thanks to @BatchSize in CoachProfile.
     * - ":sportId IS NULL OR ...": no sport selected -> the condition is always true.
     * - Spring Data builds the "SELECT count(...)" query for Page.getTotalPages() by itself.
     */
    @Query("""
            SELECT c FROM CoachProfile c JOIN FETCH c.user u
            WHERE u.status = :status
              AND c.experienceYears >= :minExperience
              AND LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE :namePattern
              AND (:sportId IS NULL OR EXISTS (SELECT s FROM c.sports s WHERE s.id = :sportId))
            ORDER BY u.lastName, u.firstName, c.id
            """)
    Page<CoachProfile> search(@Param("status") UserStatus status,
                              @Param("sportId") Long sportId,
                              @Param("namePattern") String namePattern,
                              @Param("minExperience") int minExperience,
                              Pageable pageable);

    // Coach page: WHERE id = ? AND user.status = ?, user and sports in the same SELECT.
    @EntityGraph(attributePaths = {"user", "sports"})
    Optional<CoachProfile> findByIdAndUserStatus(Long id, UserStatus status);
}
