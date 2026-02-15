/**
 * The `CreateWorkOrderRequest` class represents a data transfer object for creating a work order,
 * containing fields for customer information, contact details, and a list of equipment items.
 */
package com.finetune.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.model.entity.Equipment;

import jakarta.validation.constraints.NotEmpty;


public class CreateWorkOrderRequest {

    @NotBlank
    private String customerFirstName;

    @NotBlank
    private String customerLastName;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phone;

    @Email
    @NotBlank
    private String email;

    @NotEmpty(message = "At least one equipment item is required")
    private List<EquipmentItemRequest> equipment;

    @NotNull(message = "Promised by date is required")
    private LocalDate promisedBy;

    // --- setters normalize data ---
    public void setPhone(String phone) {
        this.phone = phone.replaceAll("\\D", "");
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    // getters + setters
    public String getCustomerFirstName() { return customerFirstName; }
    public void setCustomerFirstName(String customerFirstName) { this.customerFirstName = customerFirstName; }

    public String getCustomerLastName() { return customerLastName; }
    public void setCustomerLastName(String customerLastName) { this.customerLastName = customerLastName; }

    public String getPhone() { return phone; }

    public String getEmail() { return email; }
    
    public List<EquipmentItemRequest> getEquipment() { return equipment; }
    public void setEquipment(List<EquipmentItemRequest> equipment) { this.equipment = equipment; }
    
    public LocalDate getPromisedBy() { return promisedBy; }
    public void setPromisedBy(LocalDate promisedBy) { this.promisedBy = promisedBy; }
    
    // Deprecated: Keep for backward compatibility
    @Deprecated
    public List<EquipmentItemRequest> getSkis() { return equipment; }
    @Deprecated
    public void setSkis(List<EquipmentItemRequest> skis) { this.equipment = skis; }
    
}
