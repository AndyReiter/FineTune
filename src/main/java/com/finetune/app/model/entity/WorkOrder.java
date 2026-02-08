package com.finetune.app.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.finetune.app.model.enums.WorkOrderStatus;
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
        if (skiItem == null) {
            throw new IllegalArgumentException("SkiItem cannot be null");
        }
        if (!this.skiItems.contains(skiItem)) {
            this.skiItems.add(skiItem);
        }
        skiItem.setWorkOrder(this);
    }

    /**
     * Determines if this work order is "open" for accepting new ski items.
     * An open work order allows new items to be merged into it.
     * 
     * Open statuses: RECEIVED, IN_PROGRESS, READY_FOR_PICKUP
     * Closed statuses: COMPLETED (ready for pickup - no more items)
     * 
     * This is used to determine if new ski items should be merged into this order.
     */
    public boolean isOpen() {
        return !("COMPLETED".equals(this.status));
    }

    /**
     * Calculates and updates the work order status based on all ski items.
     * This method enforces item-driven state transitions per business rules:
     * 
     * BUSINESS RULES:
     * - RECEIVED: all items are PENDING
     * - IN_PROGRESS: at least one item is IN_PROGRESS 
     * - READY_FOR_PICKUP: all items are DONE (customer not notified)
     * - AWAITING_PICKUP: customer notified, awaiting pickup
     * - COMPLETED: all items are PICKED_UP
     * 
     * This is the ONLY method that should change work order status.
     * Manual status changes via setStatus() violate the item-driven workflow.
     * 
     * Note: AWAITING_PICKUP status is preserved once set via notification,
     * even if item statuses would normally trigger READY_FOR_PICKUP.
     */
    public void updateStatusBasedOnItems() {
        if (this.skiItems.isEmpty()) {
            this.status = WorkOrderStatus.RECEIVED.name();
            return;
        }

        // Rule: COMPLETED when all items are PICKED_UP
        boolean allPickedUp = this.skiItems.stream()
            .allMatch(item -> "PICKED_UP".equals(item.getStatus()));
        
        if (allPickedUp) {
            this.status = WorkOrderStatus.COMPLETED.name();
            return;
        }

        // Rule: Preserve COMPLETED status once set (no downgrades from COMPLETED)
        if (WorkOrderStatus.COMPLETED.name().equals(this.status)) {
            return; // Keep COMPLETED - this is a terminal state
        }

        // Rule: Preserve AWAITING_PICKUP once customer is notified
        // (Don't revert to READY_FOR_PICKUP even if all items are DONE)
        if (WorkOrderStatus.AWAITING_PICKUP.name().equals(this.status)) {
            return; // Keep AWAITING_PICKUP until pickup occurs
        }

        // Rule: READY_FOR_PICKUP when all items are DONE (and not yet notified)
        boolean allDone = this.skiItems.stream()
            .allMatch(item -> "DONE".equals(item.getStatus()));
        
        if (allDone) {
            this.status = WorkOrderStatus.READY_FOR_PICKUP.name();
            return;
        }

        // Rule: IN_PROGRESS when at least one item is IN_PROGRESS
        boolean anyInProgress = this.skiItems.stream()
            .anyMatch(item -> "IN_PROGRESS".equals(item.getStatus()));
        
        if (anyInProgress) {
            this.status = WorkOrderStatus.IN_PROGRESS.name();
            return;
        }

        // Rule: RECEIVED when all items are PENDING (default state)
        this.status = WorkOrderStatus.RECEIVED.name();
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