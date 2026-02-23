package com.finetune.app.model;

import java.util.List;
import java.util.ArrayList;
import com.finetune.app.model.Equipment.AbilityLevel;

public class Customer {

    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // Skier profile fields (optional)
    private Integer heightInches;
    private Integer weight;
    private AbilityLevel skiAbilityLevel;

    private List<WorkOrder> workOrders = new ArrayList<>();

    private List<Boot> boots = new ArrayList<>();

    private List<Equipment> equipment = new ArrayList<>();
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getHeightInches() { return heightInches; }
    public void setHeightInches(Integer heightInches) { this.heightInches = heightInches; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public AbilityLevel getSkiAbilityLevel() { return skiAbilityLevel; }
    public void setSkiAbilityLevel(AbilityLevel skiAbilityLevel) { this.skiAbilityLevel = skiAbilityLevel; }
    public void setSkiAbilityLevel(String skiAbilityLevel) {
        if (skiAbilityLevel != null) {
            this.skiAbilityLevel = AbilityLevel.valueOf(skiAbilityLevel);
        } else {
            this.skiAbilityLevel = null;
        }
    }
    public List<WorkOrder> getWorkOrders() { return workOrders; }
    public void setWorkOrders(List<WorkOrder> workOrders) { this.workOrders = workOrders; }
    public List<Boot> getBoots() { return boots; }
    public void setBoots(List<Boot> boots) { this.boots = boots; }
    public List<Equipment> getEquipment() { return equipment; }
    public void setEquipment(List<Equipment> equipment) { this.equipment = equipment; }

    // Custom methods
    public void addWorkOrder(WorkOrder workOrder) {
        if (workOrder == null) {
            throw new IllegalArgumentException("WorkOrder cannot be null");
        }
        if (!workOrders.contains(workOrder)) {
            workOrders.add(workOrder);
        }
        workOrder.setCustomer(this);
    }

    public void addBoot(Boot boot) {
        if (boot == null) {
            throw new IllegalArgumentException("Boot cannot be null");
        }
        if (!boots.contains(boot)) {
            boots.add(boot);
        }
        boot.setCustomer(this);
    }

    public Boot findMatchingBoot(String brand, String model, Integer bsl) {
        return boots.stream()
                .filter(boot -> 
                    (boot.getBrand() == null ? brand == null : boot.getBrand().equals(brand)) &&
                    (boot.getModel() == null ? model == null : boot.getModel().equals(model)) &&
                    (boot.getBsl() == null ? bsl == null : boot.getBsl().equals(bsl)))
                .findFirst()
                .orElse(null);
    }

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