package org.example.integrated_architectures.coach;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.example.integrated_architectures.sport.Sport;
import org.example.integrated_architectures.user.User;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coach_profiles")
public class CoachProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears;

    @Column(name = "hourly_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal hourlyPrice;

    private String city;

    @Column(nullable = false)
    private boolean online = true;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToMany
    @JoinTable(
            name = "coach_profile_sports",
            joinColumns = @JoinColumn(name = "coach_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "sport_id"))
    private Set<Sport> sports = new HashSet<>();

    protected CoachProfile() {
        // required by JPA
    }

    public CoachProfile(User user, int experienceYears, BigDecimal hourlyPrice) {
        this.user = user;
        this.experienceYears = experienceYears;
        this.hourlyPrice = hourlyPrice;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
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

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Set<Sport> getSports() {
        return sports;
    }
}
