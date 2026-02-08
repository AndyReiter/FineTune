/**
 * This Java class represents a SkiItem entity with attributes for ski make, model, service type, and a
 * many-to-one relationship with a WorkOrder entity.
 */
package com.finetune.app.model.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name = "ski_items")
public class SkiItem {

    public enum SkiCondition {
        NEW,
        USED
    }

    public enum SkiAbilityLevel {
        ADVANCED,
        INTERMEDIATE,
        BEGINNER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skiMake;
    private String skiModel;
    private String serviceType;

    @Enumerated(EnumType.STRING)
    private SkiCondition condition = SkiCondition.USED;  // Default for existing records

    // Mount-specific fields
    private String bindingBrand;
    private String bindingModel;
    private Integer heightInches; // Always store total inches in DB
    private Integer weight;       // lbs
    private Integer age;

    @Enumerated(EnumType.STRING)
    private SkiAbilityLevel skiAbilityLevel;

    // Boot relationship (optional - only required for MOUNT service)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boot_id", nullable = true)
    @JsonBackReference
    private Boot boot;

    /**
     * Individual status for this ski item (e.g., "PENDING", "IN_PROGRESS", "DONE")
     * This allows tracking progress on individual items within a work order.
     */
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    @JsonBackReference
    private WorkOrder workOrder;

    // Constructor
    public SkiItem() {
        this.status = "PENDING";
        this.condition = SkiCondition.USED;  // Default value for migration compatibility
    }

    public SkiItem(String skiMake, String skiModel, String serviceType) {
        this.skiMake = skiMake;
        this.skiModel = skiModel;
        this.serviceType = serviceType;
        this.status = "PENDING";
        this.condition = SkiCondition.USED;  // Default value for migration compatibility
    }

    // getters + setters
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

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }
    
    public SkiCondition getCondition() {
        return condition;
    }

    public void setCondition(SkiCondition condition) {
        this.condition = condition;
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

    public Boot getBoot() {
        return boot;
    }

    public void setBoot(Boot boot) {
        this.boot = boot;
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
}