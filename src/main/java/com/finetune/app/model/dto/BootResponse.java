package com.finetune.app.model.dto;

import com.finetune.app.model.Boot;
import com.finetune.app.model.Equipment.AbilityLevel;

/**
 * BootResponse DTO for clean JSON serialization of Boot entities.
 * Prevents circular references and provides only necessary data.
 */
public class BootResponse {
    
    private Long id;
    private String brand;
    private String model;
    private Integer bsl;
    private Integer heightInches;
    private Integer weight;
    private Integer age;
    private AbilityLevel abilityLevel;
    private Boolean active;

    // Default constructor
    public BootResponse() {}

    // Constructor with all fields
    public BootResponse(Long id, String brand, String model, Integer bsl, 
                       Integer heightInches, Integer weight, Integer age, 
                       AbilityLevel abilityLevel, Boolean active) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.bsl = bsl;
        this.heightInches = heightInches;
        this.weight = weight;
        this.age = age;
        this.abilityLevel = abilityLevel;
        this.active = active;
    }

    /**
     * Create BootResponse from Boot entity.
     * 
     * @param boot The Boot entity
     * @return BootResponse DTO
     */
    public static BootResponse fromEntity(Boot boot) {
        return new BootResponse(
            boot.getId(),
            boot.getBrand(),
            boot.getModel(),
            boot.getBsl(),
            boot.getHeightInches(),
            boot.getWeight(),
            boot.getAge(),
            boot.getAbilityLevel(),
            boot.isActive()
        );
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getBsl() {
        return bsl;
    }

    public void setBsl(Integer bsl) {
        this.bsl = bsl;
    }

    public Integer getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(Integer heightInches) {
        this.heightInches = heightInches;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public AbilityLevel getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(AbilityLevel abilityLevel) {
        this.abilityLevel = abilityLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}