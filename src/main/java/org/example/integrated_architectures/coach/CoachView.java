package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.sport.Sport;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the catalog and the coach page show about a coach (a DTO, not the entity).
 * - The template gets plain data, no lazy JPA fields (open-in-view=false).
 * - No email, status or rejection reason leaks to the public pages.
 * - The REST API (step 9) will return the same object as JSON.
 */
public record CoachView(
        Long id,
        String fullName,
        List<String> sportNames,
        int experienceYears,
        BigDecimal hourlyPrice,
        String city,
        boolean online,
        String bio) {

    // Must be called inside a transaction: it reads the lazy user and sports.
    public static CoachView from(CoachProfile profile) {
        return new CoachView(
                profile.getId(),
                profile.getUser().getFirstName() + " " + profile.getUser().getLastName(),
                profile.getSports().stream().map(Sport::getName).sorted().toList(),
                profile.getExperienceYears(),
                profile.getHourlyPrice(),
                profile.getCity(),
                profile.isOnline(),
                profile.getBio());
    }
}
