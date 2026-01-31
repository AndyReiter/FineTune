package com.finetune.app.model.dto;

import com.finetune.app.model.entity.SkiItem;

/**
 * DTO for SkiItem responses.
 * Does not include the WorkOrder reference to prevent circular references.
 */
public class SkiItemResponse {

    private Long id;
    private String skiMake;
    private String skiModel;
    private String serviceType;
    private String status;

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
}
