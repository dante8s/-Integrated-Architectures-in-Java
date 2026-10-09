package org.example.integrated_architectures.sport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data of the admin form for creating and editing a sport.
 * Limits match the columns: sports.name VARCHAR(100), sports.description VARCHAR(500).
 */
public class SportForm {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    public SportForm() {
    }

    // Fills the edit form with the current values.
    public SportForm(Sport sport) {
        this.name = sport.getName();
        this.description = sport.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
