package com.finetune.app.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating StaffSettings.
 */
public class UpdateStaffSettingsRequest {
    
    @NotNull(message = "Max customer work orders per day is required")
    @Min(value = 1, message = "Max customer work orders per day must be at least 1")
    private Integer maxCustomerWorkOrdersPerDay;

    public UpdateStaffSettingsRequest() {}

    public UpdateStaffSettingsRequest(Integer maxCustomerWorkOrdersPerDay) {
        this.maxCustomerWorkOrdersPerDay = maxCustomerWorkOrdersPerDay;
    }

    // Getters and Setters
    public Integer getMaxCustomerWorkOrdersPerDay() {
        return maxCustomerWorkOrdersPerDay;
    }

    public void setMaxCustomerWorkOrdersPerDay(Integer maxCustomerWorkOrdersPerDay) {
        this.maxCustomerWorkOrdersPerDay = maxCustomerWorkOrdersPerDay;
    }
}
