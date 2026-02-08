package com.finetune.app.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.finetune.app.model.entity.SkiItem.SkiAbilityLevel;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "boots")
public class Boot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String brand;

    @NotBlank
    @Column(nullable = false)
    private String model;

    @NotNull
    @Column(nullable = false)
    private Integer bsl;  // Boot Sole Length in mm

    @Column(nullable = true)
    private Integer heightInches;

    @Column(nullable = true)
    private Integer weight;

    @Column(nullable = true)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SkiAbilityLevel abilityLevel;

    @Column(nullable = true, columnDefinition = "boolean default true")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;

    @OneToMany(mappedBy = "boot", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonManagedReference
    private List<SkiItem> skiItems = new ArrayList<>();

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

    public Boot(String brand, String model, Integer bsl, Integer heightInches, 
                Integer weight, Integer age, SkiAbilityLevel abilityLevel) {
        this.brand = brand;
        this.model = model;
        this.bsl = bsl;
        this.heightInches = heightInches;
        this.weight = weight;
        this.age = age;
        this.abilityLevel = abilityLevel;
        this.active = true;
    }

    // Helper method to add ski item
    public void addSkiItem(SkiItem skiItem) {
        if (skiItem != null) {
            skiItems.add(skiItem);
            skiItem.setBoot(this);
        }
    }

    // Helper method to remove ski item
    public void removeSkiItem(SkiItem skiItem) {
        if (skiItem != null) {
            skiItems.remove(skiItem);
            skiItem.setBoot(null);
        }
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

    public SkiAbilityLevel getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(SkiAbilityLevel abilityLevel) {
        this.abilityLevel = abilityLevel;
    }

    public boolean isActive() {
        return active != null ? active : true; // Default to true if null
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<SkiItem> getSkiItems() {
        return skiItems;
    }

    public void setSkiItems(List<SkiItem> skiItems) {
        this.skiItems = skiItems;
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