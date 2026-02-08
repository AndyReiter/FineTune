package com.finetune.app.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for updating a ski item's status.
 * Used by PATCH /workorders/{id}/skis/{skiId}/status endpoint.
 * 
 * Enforces item-driven workflow - only allows valid manual status transitions.
 * PICKED_UP status cannot be set manually (only via pickup workflow).
 */
public class UpdateSkiItemStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "PENDING|IN_PROGRESS|DONE", 
        message = "Status must be PENDING, IN_PROGRESS, or DONE. PICKED_UP can only be set via pickup workflow."
    )
    private String status;

    public UpdateSkiItemStatusRequest() {
    }

    public UpdateSkiItemStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
