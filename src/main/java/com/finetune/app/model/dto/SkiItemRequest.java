
package com.finetune.app.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SkiItemRequest {

    @NotBlank
    private String skiMake;

    @NotBlank
    private String skiModel;

    @NotBlank
    private String serviceType;

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
    
}
