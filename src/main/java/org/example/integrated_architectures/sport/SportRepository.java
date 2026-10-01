package org.example.integrated_architectures.sport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportRepository extends JpaRepository<Sport, Long> {

    List<Sport> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
