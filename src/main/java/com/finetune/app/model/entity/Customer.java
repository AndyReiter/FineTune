package com.finetune.app.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import com.finetune.app.model.entity.Equipment.AbilityLevel;

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

    // Skier profile fields (optional)
    private Integer heightInches;
    private Integer weight;
    @Enumerated(EnumType.STRING)
    private AbilityLevel skiAbilityLevel;

    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<WorkOrder> workOrders = new ArrayList<>();

    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<Boot> boots = new ArrayList<>();

    @OneToMany(
        mappedBy = "customer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    private List<Equipment> equipment = new ArrayList<>();

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

    public Integer getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(Integer heightInches) {
        this.heightInches = heightInches;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public AbilityLevel getSkiAbilityLevel() {
        return skiAbilityLevel;
    }

    public void setSkiAbilityLevel(AbilityLevel skiAbilityLevel) {
        this.skiAbilityLevel = skiAbilityLevel;
    }

    public List<Boot> getBoots() {
        return boots;
    }

    public void setBoots(List<Boot> boots) {
        this.boots = boots;
    }

    /**
     * Adds a boot to this customer and sets the bidirectional relationship.
     */
    public void addBoot(Boot boot) {
        if (boot == null) {
            throw new IllegalArgumentException("Boot cannot be null");
        }
        if (!boots.contains(boot)) {
            boots.add(boot);
        }
        boot.setCustomer(this);
    }

    /**
     * Finds an existing boot with the same brand, model, and BSL.
     * Returns null if no matching boot is found.
     */
    public Boot findMatchingBoot(String brand, String model, Integer bsl) {
        return boots.stream()
                .filter(boot -> 
                    (boot.getBrand() == null ? brand == null : boot.getBrand().equals(brand)) &&
                    (boot.getModel() == null ? model == null : boot.getModel().equals(model)) &&
                    (boot.getBsl() == null ? bsl == null : boot.getBsl().equals(bsl)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the customer's equipment list.
     */
    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    /**
     * Adds equipment to this customer and sets the bidirectional relationship.
     * Customer owns Equipment lifecycle.
     */
    public void addEquipment(Equipment equipment) {
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }
        equipment.setCustomer(this);
        if (!this.equipment.contains(equipment)) {
            this.equipment.add(equipment);
        }
    }
}