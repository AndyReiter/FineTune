package com.finetune.app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;

@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "phone"})
    }
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<WorkOrder> workOrders = new ArrayList<>();

    public void addWorkOrder(WorkOrder workOrder) {
        if (workOrder == null) {
            throw new IllegalArgumentException("WorkOrder cannot be null");
        }
        if (!workOrders.contains(workOrder)) {
            workOrders.add(workOrder);
        }
        workOrder.setCustomer(this);
    }

    // getters/setters
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

    public List<WorkOrder> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<WorkOrder> workOrders) {
        this.workOrders = workOrders;
    }
}