package com.finetune.app.model.dto;

import com.finetune.app.model.Equipment;

/**
 * DTO for Equipment responses (for ski items).
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
     * Factory method to convert an Equipment entity to this DTO.
     */
    public static SkiItemResponse fromEntity(Equipment equipment) {
        SkiItemResponse response = new SkiItemResponse();
        response.id = equipment.getId();
        response.skiMake = equipment.getBrand();
        response.skiModel = equipment.getModel();
        response.serviceType = equipment.getServiceType();
        response.status = equipment.getStatus();
        
        // Include mount-specific data if present
        response.bindingBrand = equipment.getBindingBrand();
        response.bindingModel = equipment.getBindingModel();
        response.heightInches = equipment.getHeightInches();
        response.weight = equipment.getWeight();
        response.age = equipment.getAge();
        response.skiAbilityLevel = equipment.getAbilityLevel() != null ? 
            equipment.getAbilityLevel().name() : null;
        response.condition = equipment.getCondition() != null ? 
            equipment.getCondition().name() : null;
            
        // Include boot information if present
        if (equipment.getBoot() != null) {
            response.boot = BootResponse.fromEntity(equipment.getBoot());
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
