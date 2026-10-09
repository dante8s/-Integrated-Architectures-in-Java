package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.user.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
