package com.finetune.app.model.dto;

import com.finetune.app.model.Equipment.AbilityLevel;

public class BootRequest {
    private String brand;
    private String model;
    private Integer bsl;
    private Integer heightInches;
    private Integer weight;
    private Integer age;
    private AbilityLevel skiAbilityLevel;

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getBsl() { return bsl; }
    public void setBsl(Integer bsl) { this.bsl = bsl; }

    public Integer getHeightInches() { return heightInches; }
    public void setHeightInches(Integer heightInches) { this.heightInches = heightInches; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public AbilityLevel getSkiAbilityLevel() { return skiAbilityLevel; }
    public void setSkiAbilityLevel(AbilityLevel skiAbilityLevel) { this.skiAbilityLevel = skiAbilityLevel; }
}