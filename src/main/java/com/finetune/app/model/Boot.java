package com.finetune.app.model;

import com.finetune.app.model.Equipment.AbilityLevel;
import java.util.List;
import java.util.ArrayList;

public class Boot {
    // Constructor matching WorkOrderService usage
    public Boot(String brand, String model, Integer bsl, Integer heightInches, Integer weight, Integer age, AbilityLevel abilityLevel) {
        this.brand = brand;
        this.model = model;
        this.bsl = bsl;
        this.heightInches = heightInches;
        this.weight = weight;
        this.age = age;
        this.abilityLevel = abilityLevel;
        this.active = true;
    }

    private Long id;

    private String brand;

    private String model;

    private Integer bsl;  // Boot Sole Length in mm

    private Integer heightInches;

    private Integer weight;

    private Integer age;

    private AbilityLevel abilityLevel;

    private Boolean active = true;

    private Customer customer;

    private List<Equipment> equipment = new ArrayList<>();

    // Constructors
    public Boot() {
    }

    // Legacy constructor for backward compatibility
    public Boot(String brand, String model, Integer bsl) {
        this.brand = brand;
        this.model = model;
        this.bsl = bsl;
        this.active = true; // Default value
    }

    // Helper method to add equipment
    public void addEquipment(Equipment equipmentItem) {
        if (equipmentItem != null) {
            equipment.add(equipmentItem);
            equipmentItem.setBoot(this);
        }
    }
    public void setAbilityLevel(String abilityLevel) {
        if (abilityLevel != null) {
            this.abilityLevel = AbilityLevel.valueOf(abilityLevel);
        } else {
            this.abilityLevel = null;
        }
    }

    // Helper method to remove equipment
    public void removeEquipment(Equipment equipmentItem) {
        if (equipmentItem != null) {
            equipment.remove(equipmentItem);
            equipmentItem.setBoot(null);
        }
    }

    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }
    public void setCustomerId(Long customerId) {
        if (customer == null) customer = new Customer();
        customer.setId(customerId);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getBsl() {
        return bsl;
    }

    public void setBsl(Integer bsl) {
        this.bsl = bsl;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public AbilityLevel getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(AbilityLevel abilityLevel) {
        this.abilityLevel = abilityLevel;
    }

    public boolean isActive() {
        return active != null ? active : true; // Default to true if null
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // getCustomer and setCustomer methods already defined earlier; removed duplicates

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    // Equals and hashCode based on brand, model, BSL, and customer for finding duplicates
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Boot boot = (Boot) o;
        return brand != null ? brand.equals(boot.brand) : boot.brand == null &&
               model != null ? model.equals(boot.model) : boot.model == null &&
               bsl != null ? bsl.equals(boot.bsl) : boot.bsl == null &&
               customer != null ? customer.equals(boot.customer) : boot.customer == null;
    }

    @Override
    public int hashCode() {
        int result = brand != null ? brand.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (bsl != null ? bsl.hashCode() : 0);
        result = 31 * result + (customer != null ? customer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Boot{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", bsl=" + bsl +
                ", heightInches=" + heightInches +
                ", weight=" + weight +
                ", age=" + age +
                ", abilityLevel=" + abilityLevel +
                ", active=" + active +
                '}';
    }
}