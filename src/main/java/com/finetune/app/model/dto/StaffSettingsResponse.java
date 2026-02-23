package com.finetune.app.model.dto;

import com.finetune.app.model.StaffSettings;

/**
 * DTO for StaffSettings responses.
 */
public class StaffSettingsResponse {
    
    private Long id;
    private Integer maxCustomerWorkOrdersPerDay;

    public StaffSettingsResponse() {}

    public static StaffSettingsResponse fromEntity(StaffSettings settings) {
        StaffSettingsResponse response = new StaffSettingsResponse();
        response.id = settings.getId();
        response.maxCustomerWorkOrdersPerDay = settings.getMaxCustomerWorkOrdersPerDay();
        return response;
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
}
