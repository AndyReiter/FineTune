package com.finetune.app.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.finetune.app.model.enums.WorkOrderStatus;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(name = "promised_by", nullable = false)
    private LocalDate promisedBy;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @OneToMany(
        mappedBy = "workOrder",
        fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<Equipment> equipment = new ArrayList<>();

    @OneToMany(
        mappedBy = "workOrder",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL
    )
    @OrderBy("createdAt DESC")
    private List<WorkOrderNote> notes = new ArrayList<>();

    public void addEquipment(Equipment equipmentItem) {
        if (equipmentItem == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }
        if (!this.equipment.contains(equipmentItem)) {
            this.equipment.add(equipmentItem);
        }
        equipmentItem.setWorkOrder(this);
    }

    /**
     * Determines if this work order is "open" for accepting new equipment items.
     * An open work order allows new items to be merged into it.
     * 
     * Open statuses: RECEIVED, IN_PROGRESS, READY_FOR_PICKUP
     * Closed statuses: COMPLETED (ready for pickup - no more items)
     * 
     * This is used to determine if new equipment items should be merged into this order.
     */
    public boolean isOpen() {
        return !("COMPLETED".equals(this.status));
    }

    /**
     * Calculates and updates the work order status based on all equipment items.
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
        if (this.equipment.isEmpty()) {
            this.status = WorkOrderStatus.RECEIVED.name();
            return;
        }

        // Rule: COMPLETED when all items are PICKED_UP
        boolean allPickedUp = this.equipment.stream()
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
        boolean allDone = this.equipment.stream()
            .allMatch(item -> "DONE".equals(item.getStatus()));
        
        if (allDone) {
            this.status = WorkOrderStatus.READY_FOR_PICKUP.name();
            return;
        }

        // Rule: IN_PROGRESS when at least one item is IN_PROGRESS
        boolean anyInProgress = this.equipment.stream()
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

    public LocalDate getPromisedBy() {
        return promisedBy;
    }

    public void setPromisedBy(LocalDate promisedBy) {
        this.promisedBy = promisedBy;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    public List<WorkOrderNote> getNotes() {
        return notes;
    }

    public void setNotes(List<WorkOrderNote> notes) {
        this.notes = notes;
    }
}