package com.finetune.app.model.dto;

import com.finetune.app.model.entity.Equipment;
import com.finetune.app.model.entity.Equipment.EquipmentType;
import com.finetune.app.model.entity.Equipment.Condition;
import com.finetune.app.model.entity.Equipment.AbilityLevel;

/**
 * DTO for Equipment responses.
 * Returns equipment information without work order references to prevent circular dependencies.
 * Includes all fields needed for work order equipment display including mount service details.
 */
public class EquipmentResponse {

    private Long id;
    private EquipmentType type;
    private String brand;
    private String model;
    private Integer length;
    private String serviceType;
    private String status;
    private Condition condition;
    private AbilityLevel abilityLevel;
    
    // Mount-specific fields
    private String bindingBrand;
    private String bindingModel;
    private Integer heightInches;
    private Integer weight;
    private Integer age;
    
    // Boot information (when applicable)
    private BootResponse boot;

    // Constructors
    public EquipmentResponse() {
    }

    /**
     * Factory method to convert an Equipment entity to this DTO.
     */
    public static EquipmentResponse fromEntity(Equipment equipment) {
        EquipmentResponse response = new EquipmentResponse();
        response.id = equipment.getId();
        response.type = equipment.getType();
        response.brand = equipment.getBrand();
        response.model = equipment.getModel();
        response.length = equipment.getLength();
        response.serviceType = equipment.getServiceType();
        response.status = equipment.getStatus();
        response.condition = equipment.getCondition();
        response.abilityLevel = equipment.getAbilityLevel();
        
        // Include mount-specific data if present
        response.bindingBrand = equipment.getBindingBrand();
        response.bindingModel = equipment.getBindingModel();
        response.heightInches = equipment.getHeightInches();
        response.weight = equipment.getWeight();
        response.age = equipment.getAge();
        
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

    public BootResponse getBoot() {
        return boot;
    }

    public void setBoot(BootResponse boot) {
        this.boot = boot;
    }
}
