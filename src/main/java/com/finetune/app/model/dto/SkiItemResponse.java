package com.finetune.app.model.dto;

import com.finetune.app.model.entity.SkiItem;

/**
 * DTO for SkiItem responses.
 * Does not include the WorkOrder reference to prevent circular references.
 * Includes boot information for mount services.
 */
public class SkiItemResponse {

    private Long id;
    private String skiMake;
    private String skiModel;
    private String serviceType;
    private String status;
    
    // Mount-specific fields
    private String bindingBrand;
    private String bindingModel;
    private Integer heightInches;
    private Integer weight;
    private Integer age;
    private String skiAbilityLevel;
    private String condition;
    
    // Boot information (when applicable)
    private BootResponse boot;

    public SkiItemResponse() {}

    /**
     * Factory method to convert a SkiItem entity to this DTO.
     */
    public static SkiItemResponse fromEntity(SkiItem skiItem) {
        SkiItemResponse response = new SkiItemResponse();
        response.id = skiItem.getId();
        response.skiMake = skiItem.getSkiMake();
        response.skiModel = skiItem.getSkiModel();
        response.serviceType = skiItem.getServiceType();
        response.status = skiItem.getStatus();
        
        // Include mount-specific data if present
        response.bindingBrand = skiItem.getBindingBrand();
        response.bindingModel = skiItem.getBindingModel();
        response.heightInches = skiItem.getHeightInches();
        response.weight = skiItem.getWeight();
        response.age = skiItem.getAge();
        response.skiAbilityLevel = skiItem.getSkiAbilityLevel() != null ? 
            skiItem.getSkiAbilityLevel().name() : null;
        response.condition = skiItem.getCondition() != null ? 
            skiItem.getCondition().name() : null;
            
        // Include boot information if present
        if (skiItem.getBoot() != null) {
            response.boot = BootResponse.fromEntity(skiItem.getBoot());
        }
        
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getSkiAbilityLevel() {
        return skiAbilityLevel;
    }

    public void setSkiAbilityLevel(String skiAbilityLevel) {
        this.skiAbilityLevel = skiAbilityLevel;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BootResponse getBoot() {
        return boot;
    }

    public void setBoot(BootResponse boot) {
        this.boot = boot;
    }
}
