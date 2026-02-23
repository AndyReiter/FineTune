package com.finetune.app.model.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for equipment options in public work order response.
 */
public class EquipmentOptionResponse {
    
    private Long skiId;
    private String skiName;
    private List<AssociatedBootResponse> associatedBoots = new ArrayList<>();

    public EquipmentOptionResponse() {}

    public EquipmentOptionResponse(Long skiId, String skiName) {
        this.skiId = skiId;
        this.skiName = skiName;
    }

    // Getters and Setters
    public Long getSkiId() {
        return skiId;
    }

    public void setSkiId(Long skiId) {
        this.skiId = skiId;
    }

    public String getSkiName() {
        return skiName;
    }

    public void setSkiName(String skiName) {
        this.skiName = skiName;
    }

    public List<AssociatedBootResponse> getAssociatedBoots() {
        return associatedBoots;
    }

    public void setAssociatedBoots(List<AssociatedBootResponse> associatedBoots) {
        this.associatedBoots = associatedBoots;
    }

    public void addAssociatedBoot(AssociatedBootResponse boot) {
        this.associatedBoots.add(boot);
    }
}
