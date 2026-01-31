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
     * Status is DONE only when all ski items have status "DONE".
     * This ensures the order is only marked complete when all work is finished.
     */
    public void updateStatusBasedOnItems() {
        if (this.skiItems.isEmpty()) {
            this.status = "RECEIVED";
            return;
        }

        boolean allDone = this.skiItems.stream()
            .allMatch(item -> "DONE".equals(item.getStatus()));

        if (allDone) {
            this.status = "DONE";
        }
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