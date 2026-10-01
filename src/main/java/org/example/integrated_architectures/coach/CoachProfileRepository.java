package org.example.integrated_architectures.coach;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long> {

    Optional<CoachProfile> findByUserId(Long userId);
}
