package com.finetune.app.model.dto;

import com.finetune.app.model.Equipment.EquipmentType;
import com.finetune.app.model.Equipment.Condition;
import com.finetune.app.model.Equipment.AbilityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for creating Equipment directly on a Customer profile.
 * This allows customers to register their equipment independent of work orders.
 */
public class EquipmentRequest {

    private EquipmentType type;  // Default to SKI if null

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @Positive(message = "Length must be positive")
    private Integer length;

    @NotNull(message = "Condition is required")
    private Condition condition;

    @NotNull(message = "Ability level is required")
    private AbilityLevel abilityLevel;

    // Constructors
    public EquipmentRequest() {
    }

    // Getters and Setters
    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public AbilityLevel getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(AbilityLevel abilityLevel) {
        this.abilityLevel = abilityLevel;
    }
}
