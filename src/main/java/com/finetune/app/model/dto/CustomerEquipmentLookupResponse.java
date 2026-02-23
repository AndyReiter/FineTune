package com.finetune.app.model.dto;

import com.finetune.app.model.Customer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for public customer equipment lookup response.
 * Returns customer's equipment and boots for selection in public work order workflow.
 */
public class CustomerEquipmentLookupResponse {
    
    private Long customerId;
    private String customerName;
    private List<EquipmentResponse> equipment;
    private List<BootResponse> boots;

    public CustomerEquipmentLookupResponse() {}

    public static CustomerEquipmentLookupResponse fromEntity(Customer customer) {
        CustomerEquipmentLookupResponse response = new CustomerEquipmentLookupResponse();
        response.customerId = customer.getId();
        response.customerName = customer.getFirstName() + " " + customer.getLastName();
        
        response.equipment = customer.getEquipment()
            .stream()
            .map(EquipmentResponse::fromEntity)
            .collect(Collectors.toList());
        
        response.boots = customer.getBoots()
            .stream()
            .map(BootResponse::fromEntity)
            .collect(Collectors.toList());
        
        return response;
    }

    // Getters and Setters
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

    public List<EquipmentResponse> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentResponse> equipment) {
        this.equipment = equipment;
    }

    public List<BootResponse> getBoots() {
        return boots;
    }

    public void setBoots(List<BootResponse> boots) {
        this.boots = boots;
    }
}
