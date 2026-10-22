package org.example.integrated_architectures.coach;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.integrated_architectures.sport.Sport;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Edit my profile" form of a coach. The same rules as the profile part of CoachRegistrationForm.
 * Name, email and password are not edited here (future work).
 */
public class CoachProfileForm {

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 80, message = "Experience looks too large")
    private Integer experienceYears;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Max 6 digits before and 2 after the decimal point")
    private BigDecimal hourlyPrice;

    @Size(max = 100)
    private String city;

    @Size(max = 2000, message = "Bio is limited to 2000 characters")
    private String bio;

    @NotEmpty(message = "Choose at least one sport")
    private Set<Long> sportIds = new HashSet<>();

    // The form opens with the current values of the profile. Must be called inside a transaction (lazy sports).
    static CoachProfileForm from(CoachProfile profile) {
        CoachProfileForm form = new CoachProfileForm();
        form.setExperienceYears(profile.getExperienceYears());
        form.setHourlyPrice(profile.getHourlyPrice());
        form.setCity(profile.getCity());
        form.setBio(profile.getBio());
        form.setSportIds(profile.getSports().stream().map(Sport::getId).collect(Collectors.toSet()));
        return form;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getHourlyPrice() {
        return hourlyPrice;
    }

    public void setHourlyPrice(BigDecimal hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<Long> getSportIds() {
        return sportIds;
    }

    public void setSportIds(Set<Long> sportIds) {
        this.sportIds = sportIds;
    }
}
