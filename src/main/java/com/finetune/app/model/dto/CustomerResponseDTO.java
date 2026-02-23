package com.finetune.app.model.dto;

import com.finetune.app.model.Customer;

/**
 * DTO for Customer API responses with aggregate counts.
 * Used for customer list and detail views.
 */
public class CustomerResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int workOrderCount;
    private int equipmentCount;
    private int bootCount;

    public CustomerResponseDTO() {}

    /**
     * Factory method to convert a Customer entity to this DTO.
     * Calculates counts from the entity relationships.
     */
    public static CustomerResponseDTO fromEntity(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.id = customer.getId();
        dto.firstName = customer.getFirstName();
        dto.lastName = customer.getLastName();
        dto.email = customer.getEmail();
        dto.phone = customer.getPhone();
        dto.workOrderCount = customer.getWorkOrders() != null ? customer.getWorkOrders().size() : 0;
        dto.equipmentCount = customer.getEquipment() != null ? customer.getEquipment().size() : 0;
        dto.bootCount = customer.getBoots() != null ? customer.getBoots().size() : 0;
        return dto;
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

    public int getWorkOrderCount() {
        return workOrderCount;
    }

    public void setWorkOrderCount(int workOrderCount) {
        this.workOrderCount = workOrderCount;
    }

    public int getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(int equipmentCount) {
        this.equipmentCount = equipmentCount;
    }

    public int getBootCount() {
        return bootCount;
    }

    public void setBootCount(int bootCount) {
        this.bootCount = bootCount;
    }
}
