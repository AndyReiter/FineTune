package com.finetune.app.model.dto;

/**
 * Request body for updating a ski item's status.
 * Used by PATCH /workorders/{id}/skis/{skiId}/status endpoint.
 */
public class UpdateSkiItemStatusRequest {
    
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
