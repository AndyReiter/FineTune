/**
 * The `CreateWorkOrderRequest` class represents a data transfer object for creating a work order,
 * containing fields for customer information, contact details, and a list of ski items.
 */
package com.finetune.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.model.entity.SkiItem;

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

    @NotEmpty(message = "At least one ski is required")
    private List<SkiItemRequest> skis;

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
    public List<SkiItemRequest> getSkis() { return skis; }
    public void setSkis(List<SkiItemRequest> skis) { this.skis = skis; }
    
}
