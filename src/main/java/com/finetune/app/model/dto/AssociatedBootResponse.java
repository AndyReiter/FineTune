package com.finetune.app.model.dto;

/**
 * DTO for associated boot information in public work order response.
 */
public class AssociatedBootResponse {
    
    private Long bootId;
    private String bootName;

    public AssociatedBootResponse() {}

    public AssociatedBootResponse(Long bootId, String bootName) {
        this.bootId = bootId;
        this.bootName = bootName;
    }

    // Getters and Setters
    public Long getBootId() {
        return bootId;
    }

    public void setBootId(Long bootId) {
        this.bootId = bootId;
    }

    public String getBootName() {
        return bootName;
    }

    public void setBootName(String bootName) {
        this.bootName = bootName;
    }
}
