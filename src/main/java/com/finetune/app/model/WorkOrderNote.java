package com.finetune.app.model;

import java.time.LocalDateTime;

public class WorkOrderNote {

    private Long id;

    private Long workOrderId;

    private WorkOrder workOrder;

    private String noteText;

    private String createdBy;

    private LocalDateTime createdAt;

    // Remove JPA lifecycle methods; set timestamps manually in service/repository if needed

    // Constructors
    public WorkOrderNote() {
    }

    public WorkOrderNote(Long workOrderId, String noteText, String createdBy) {
        this.workOrderId = workOrderId;
        this.noteText = noteText;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
        if (workOrder != null) {
            this.workOrderId = workOrder.getId();
        }
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
