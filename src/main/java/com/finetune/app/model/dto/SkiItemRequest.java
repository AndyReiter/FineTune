
package com.finetune.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import com.finetune.app.model.entity.SkiItem.SkiCondition;
import com.finetune.app.model.entity.SkiItem.SkiAbilityLevel;

public class SkiItemRequest {

    @NotBlank
    private String skiMake;

    @NotBlank
    private String skiModel;

    @NotBlank
    private String serviceType;

    // Mount-specific fields (optional for non-mount services)
    private String bindingBrand;
    private String bindingModel;
    private Long bootId;          // ID of existing boot to use
    private String bootBrand;
    private String bootModel;
    private Integer bsl;          // Boot Sole Length in mm
    private Integer heightInches; // Total height in inches
    private Integer weight;       // Weight in lbs
    private Integer age;
    private SkiAbilityLevel skiAbilityLevel;
    private SkiCondition condition;

    // getters + setters
    public String getSkiMake() {
        return skiMake;
    }

    public void setSkiMake(String skiMake) {
        this.skiMake = skiMake;
    }

    public String getSkiModel() {
        return skiModel;
    }

    public void setSkiModel(String skiModel) {
        this.skiModel = skiModel;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getBindingBrand() {
        return bindingBrand;
    }

    public void setBindingBrand(String bindingBrand) {
        this.bindingBrand = bindingBrand;
    }

    public String getBindingModel() {
        return bindingModel;
    }

    public void setBindingModel(String bindingModel) {
        this.bindingModel = bindingModel;
    }

    public Long getBootId() {
        return bootId;
    }

    public void setBootId(Long bootId) {
        this.bootId = bootId;
    }

    public String getBootBrand() {
        return bootBrand;
    }

    public void setBootBrand(String bootBrand) {
        this.bootBrand = bootBrand;
    }

    public String getBootModel() {
        return bootModel;
    }

    public void setBootModel(String bootModel) {
        this.bootModel = bootModel;
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

    public SkiAbilityLevel getSkiAbilityLevel() {
        return skiAbilityLevel;
    }

    public void setSkiAbilityLevel(SkiAbilityLevel skiAbilityLevel) {
        this.skiAbilityLevel = skiAbilityLevel;
    }

    public SkiCondition getCondition() {
        return condition;
    }

    public void setCondition(SkiCondition condition) {
        this.condition = condition;
    }
    
}
