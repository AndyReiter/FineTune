package com.finetune.app.model;

import java.time.LocalDateTime;

/**
 * StaffSettings domain model stores global application settings for staff and customer interactions.
 * This includes limits, preferences, and configurable parameters that affect system behavior.
 */
public class StaffSettings {

    private Long id;

    private Integer maxCustomerWorkOrdersPerDay = 25;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    // Remove JPA lifecycle methods; set timestamps manually in service/repository if needed

    // Constructors
    public StaffSettings() {}

    public StaffSettings(Integer maxCustomerWorkOrdersPerDay) {
        this.maxCustomerWorkOrdersPerDay = maxCustomerWorkOrdersPerDay;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxCustomerWorkOrdersPerDay() {
        return maxCustomerWorkOrdersPerDay;
    }

    public void setMaxCustomerWorkOrdersPerDay(Integer maxCustomerWorkOrdersPerDay) {
        this.maxCustomerWorkOrdersPerDay = maxCustomerWorkOrdersPerDay;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
