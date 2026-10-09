package org.example.integrated_architectures.sport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SportRepository extends JpaRepository<Sport, Long> {

    List<Sport> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    // For editing: is the name taken by ANOTHER sport (the edited one itself doesn't count)?
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * JPQL (entity names, not table names). Written by hand because the sport -> coach link
     * lives only in CoachProfile.sports; this way the sport package doesn't import the coach package.
     */
    @Query("select count(c) > 0 from CoachProfile c join c.sports s where s.id = :sportId")
    boolean isTaughtByAnyCoach(Long sportId);
}
