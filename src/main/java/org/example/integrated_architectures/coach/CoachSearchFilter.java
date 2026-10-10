package org.example.integrated_architectures.coach;

/**
 * Filter of the coach catalog. Filled by Spring from the GET query string:
 * /coaches?sportId=1&name=ivan&minExperience=3
 * Every field is optional: null (or empty) = "don't filter by it".
 */
public class CoachSearchFilter {

    private Long sportId;
    private String name;
    private Integer minExperience;

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(Integer minExperience) {
        this.minExperience = minExperience;
    }
}
