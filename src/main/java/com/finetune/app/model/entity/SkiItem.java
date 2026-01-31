/**
 * This Java class represents a SkiItem entity with attributes for ski make, model, service type, and a
 * many-to-one relationship with a WorkOrder entity.
 */
package com.finetune.app.model.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name = "ski_items")
public class SkiItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skiMake;
    private String skiModel;
    private String serviceType;

    /**
     * Individual status for this ski item (e.g., "PENDING", "IN_PROGRESS", "DONE")
     * This allows tracking progress on individual items within a work order.
     */
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    @JsonBackReference
    private WorkOrder workOrder;

    // Constructor
    public SkiItem() {
        this.status = "PENDING";
    }

    public SkiItem(String skiMake, String skiModel, String serviceType) {
        this.skiMake = skiMake;
        this.skiModel = skiModel;
        this.serviceType = serviceType;
        this.status = "PENDING";
    }

    // getters + setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkiMake() {
        return skiMake;
    }

    public void setSkiMake(String skiMake) {
        this.skiMake = skiMake;
    }

    public String getSkiModel() {
        return skiModel;
    }

    public void setSkiModel(String skiModel) {
        this.skiModel = skiModel;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }
}