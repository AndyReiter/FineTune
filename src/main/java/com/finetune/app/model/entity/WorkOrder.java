package com.finetune.app.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore 
    private Customer customer;

    private String status;

    private LocalDateTime createdAt;

    @OneToMany(
        mappedBy = "workOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<SkiItem> skiItems = new ArrayList<>();

    public void addSkiItem(SkiItem skiItem) {
        skiItem.setWorkOrder(this);
        this.skiItems.add(skiItem);
    }

    /**
     * Determines if this work order is "open" (has items that haven't been picked up).
     * An open work order has status != "PICKED_UP".
     * This is used to determine if new ski items should be merged into this order.
     */
    public boolean isOpen() {
        return !("PICKED_UP".equals(this.status));
    }

    /**
     * Updates the work order status based on all ski items.
     * Logic:
     * - Status is "COMPLETED" when all ski items are "PICKED_UP"
     * - Status is "READY_FOR_PICKUP" when all ski items are "DONE" 
     * - Status is "IN_PROGRESS" when any ski item is "IN_PROGRESS" or "DONE"
     * - Status remains "RECEIVED" when all items are "PENDING"
     * This ensures proper workflow progression and immediate status updates.
     */
    public void updateStatusBasedOnItems() {
        if (this.skiItems.isEmpty()) {
            this.status = "RECEIVED";
            return;
        }

        // Check if all items are PICKED_UP -> COMPLETED
        boolean allPickedUp = this.skiItems.stream()
            .allMatch(item -> "PICKED_UP".equals(item.getStatus()));
        
        if (allPickedUp) {
            this.status = "COMPLETED";
            return;
        }

        // Check if all items are DONE -> READY_FOR_PICKUP
        boolean allDone = this.skiItems.stream()
            .allMatch(item -> "DONE".equals(item.getStatus()));
        
        if (allDone) {
            this.status = "READY_FOR_PICKUP";
            return;
        }

        // Check if any item is IN_PROGRESS or DONE -> IN_PROGRESS
        boolean anyInProgressOrDone = this.skiItems.stream()
            .anyMatch(item -> "IN_PROGRESS".equals(item.getStatus()) || "DONE".equals(item.getStatus()));
        
        if (anyInProgressOrDone) {
            this.status = "IN_PROGRESS";
            return;
        }

        // All items are PENDING -> RECEIVED
        this.status = "RECEIVED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public List<SkiItem> getSkiItems() {
        return skiItems;
    }

    public void setSkiItems(List<SkiItem> skiItems) {
        this.skiItems = skiItems;
    }
}