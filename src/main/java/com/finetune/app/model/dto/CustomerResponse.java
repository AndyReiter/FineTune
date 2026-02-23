package com.finetune.app.model.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.finetune.app.model.Customer;

/**
 * DTO for Customer responses.
 * Includes basic customer info and work order summaries (without full ski items to avoid deep nesting).
 */
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<WorkOrderSummary> workOrders;

    public CustomerResponse() {}

    /**
     * Factory method to convert a Customer entity to this DTO.
     * Includes full WorkOrderResponse details for each work order.
     */
    public static CustomerResponse fromEntity(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.id = customer.getId();
        response.firstName = customer.getFirstName();
        response.lastName = customer.getLastName();
        response.email = customer.getEmail();
        response.phone = customer.getPhone();
        
        response.workOrders = customer.getWorkOrders().stream()
            .map(WorkOrderSummary::fromEntity)
            .collect(Collectors.toList());
        
        return response;
    }

    /**
     * Alternative factory that includes full WorkOrderResponse with ski items.
     */
    public static CustomerResponse fromEntityWithDetails(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.id = customer.getId();
        response.firstName = customer.getFirstName();
        response.lastName = customer.getLastName();
        response.email = customer.getEmail();
        response.phone = customer.getPhone();
        
        // Note: We don't include full ski items here to prevent deep nesting.
        // Use individual getWorkOrder endpoints for detailed ski item data.
        response.workOrders = customer.getWorkOrders().stream()
            .map(WorkOrderSummary::fromEntity)
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<WorkOrderSummary> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<WorkOrderSummary> workOrders) {
        this.workOrders = workOrders;
    }

    /**
     * Lightweight summary of a work order, without full ski item details.
     */
    public static class WorkOrderSummary {
        private Long id;
        private String status;
        private String createdAt;
        private String promisedBy;
        private Integer equipmentCount;

        public WorkOrderSummary() {}

        public static WorkOrderSummary fromEntity(com.finetune.app.model.WorkOrder workOrder) {
            WorkOrderSummary summary = new WorkOrderSummary();
            summary.id = workOrder.getId();
            summary.status = workOrder.getStatus();
            summary.createdAt = workOrder.getCreatedAt() != null ? workOrder.getCreatedAt().toString() : null;
            summary.promisedBy = workOrder.getPromisedBy() != null ? workOrder.getPromisedBy().toString() : null;
            summary.equipmentCount = workOrder.getEquipment() != null ? workOrder.getEquipment().size() : 0;
            return summary;
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

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getPromisedBy() {
            return promisedBy;
        }

        public void setPromisedBy(String promisedBy) {
            this.promisedBy = promisedBy;
        }

        public Integer getEquipmentCount() {
            return equipmentCount;
        }

        public void setEquipmentCount(Integer equipmentCount) {
            this.equipmentCount = equipmentCount;
        }
    }
}
