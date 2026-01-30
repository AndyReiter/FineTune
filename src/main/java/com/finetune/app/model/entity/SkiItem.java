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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    @JsonBackReference
    private WorkOrder workOrder;

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

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }
}