package com.finetune.app.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.finetune.app.model.entity.WorkOrder;

/**
 * DTO for WorkOrder responses.
 * Prevents infinite JSON recursion by not including the full Customer object.
 */
public class WorkOrderResponse {

    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<SkiItemResponse> skiItems;

    public WorkOrderResponse() {}

    /**
     * Factory method to convert a WorkOrder entity to this DTO.
     */
    public static WorkOrderResponse fromEntity(WorkOrder workOrder) {
        WorkOrderResponse response = new WorkOrderResponse();
        response.id = workOrder.getId();
        response.status = workOrder.getStatus();
        response.createdAt = workOrder.getCreatedAt();
        
        if (workOrder.getCustomer() != null) {
            response.customerId = workOrder.getCustomer().getId();
            response.customerName = workOrder.getCustomer().getFirstName() + " " 
                + workOrder.getCustomer().getLastName();
            response.customerEmail = workOrder.getCustomer().getEmail();
            response.customerPhone = workOrder.getCustomer().getPhone();
        }
        
        response.skiItems = workOrder.getSkiItems().stream()
            .map(SkiItemResponse::fromEntity)
            .collect(Collectors.toList());
        
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public List<SkiItemResponse> getSkiItems() {
        return skiItems;
    }

    public void setSkiItems(List<SkiItemResponse> skiItems) {
        this.skiItems = skiItems;
    }
}
