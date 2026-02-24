/**
 * This Java class represents an Equipment domain model that can be either SKI or SNOWBOARD.
 * Supports multiple service types with appropriate attributes for each equipment type.
 */
package com.finetune.app.model;

public class Equipment {

    public enum EquipmentType {
        SKI,
        SNOWBOARD
    }

    public enum Condition {
        NEW,
        USED
    }

    public enum AbilityLevel {
        ADVANCED,
        INTERMEDIATE,
        BEGINNER,
        EXPERT
    }

    private Long id;
    private EquipmentType type = EquipmentType.SKI;  // Default to SKI

    private String brand;
    private String model;
    private Integer length;
    private String serviceType;

    private Condition condition = Condition.USED;  // Default for existing records

    // Mount-specific fields
    private String bindingBrand;
    private String bindingModel;
    private Integer heightInches; // Always store total inches in DB
    private Integer weight;       // lbs
    private Integer age;

    private AbilityLevel abilityLevel;

   public void setCondition(String condition) {
       if (condition != null) {
           this.condition = Condition.valueOf(condition);
       } else {
           this.condition = null;
       }
   }
    // Boot relationship (optional - only required for MOUNT service)
    private Boot boot;

    /**
     * Individual status for this equipment item (e.g., "PENDING", "IN_PROGRESS", "DONE")
     * This allows tracking progress on individual items within a work order.
     */
    private String status = "PENDING";

    // Service history tracking
    private java.time.LocalDate lastServicedDate;
    private String lastServiceType;

    private WorkOrder workOrder;

    private Customer customer;

    // Tracks the last work order this equipment was attached to (helps auditing/history)
    private Long lastWorkOrderId;

    // Constructor
    public Equipment() {
        this.status = "PENDING";
        this.condition = Condition.USED;  // Default value for migration compatibility
        this.type = EquipmentType.SKI;    // Default to SKI
    }

    public void setAbilityLevel(String abilityLevel) {
        if (abilityLevel != null) {
            this.abilityLevel = AbilityLevel.valueOf(abilityLevel);
        } else {
            this.abilityLevel = null;
        }
    }
    public Equipment(String brand, String model, String serviceType) {
        this.brand = brand;
        this.model = model;
        this.serviceType = serviceType;
        this.status = "PENDING";
        this.condition = Condition.USED;
        this.type = EquipmentType.SKI;
    }
    public Long getBootId() {
        return boot != null ? boot.getId() : null;
    }
    public void setBootId(Long bootId) {
        if (boot == null) boot = new Boot();
        boot.setId(bootId);
    }

    // getters + setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

   public void setLastServicedDate(java.sql.Date lastServicedDate) {
       this.lastServicedDate = lastServicedDate != null ? lastServicedDate.toLocalDate() : null;
   }
    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

   public Long getWorkOrderId() {
       return workOrder != null ? workOrder.getId() : null;
   }
   public void setWorkOrderId(Long workOrderId) {
       if (workOrder == null) workOrder = new WorkOrder();
       workOrder.setId(workOrderId);
   }
    /**
     * Deprecated: Equipment no longer stores `work_order_id` directly. Use
     * `WorkOrderItem` association instead (via repository layer).
     * This setter is retained temporarily for migration compatibility.
     */
    @Deprecated
    public void setWorkOrderIdDeprecated(Long workOrderId) {
        if (workOrder == null) workOrder = new WorkOrder();
        workOrder.setId(workOrderId);
    }
     public Long getLastWorkOrderId() {
         return lastWorkOrderId;
     }
     public void setLastWorkOrderId(Long lastWorkOrderId) {
         this.lastWorkOrderId = lastWorkOrderId;
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
    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
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
    
    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getBindingBrand() {
        return bindingBrand;
    }

    public void setBindingBrand(String bindingBrand) {
        this.bindingBrand = bindingBrand;
    }

    public String getBindingModel() {
        return bindingModel;
    }

    public void setBindingModel(String bindingModel) {
        this.bindingModel = bindingModel;
    }

    public Boot getBoot() {
        return boot;
    }

    public void setBoot(Boot boot) {
        this.boot = boot;
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

    // getCustomer and setCustomer methods already defined earlier; removed duplicates

    public java.time.LocalDate getLastServicedDate() {
        return lastServicedDate;
    }

    public void setLastServicedDate(java.time.LocalDate lastServicedDate) {
        this.lastServicedDate = lastServicedDate;
    }

    public String getLastServiceType() {
        return lastServiceType;
    }

    public void setLastServiceType(String lastServiceType) {
        this.lastServiceType = lastServiceType;
    }

    /**
     * equals() and hashCode() implementation for collection membership checks.
     * 
     * If id is present (entity is persisted): use id for equality
     * Otherwise (transient entity): use business key (brand + model + length + customer)
     * 
     * This prevents duplicate equipment entries in collections before database flush.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Equipment equipment = (Equipment) o;
        
        // If both have IDs, compare by ID (persisted entities)
        if (id != null && equipment.id != null) {
            return id.equals(equipment.id);
        }
        
        // For transient entities (no ID yet), use business key
        // Compare: brand, model, length, and customer
        if (brand == null || equipment.brand == null) return false;
        if (model == null || equipment.model == null) return false;
        if (customer == null || equipment.customer == null) return false;
        
        boolean brandMatch = brand.equals(equipment.brand);
        boolean modelMatch = model.equals(equipment.model);
        boolean customerMatch = customer.getId() != null && 
                               equipment.customer.getId() != null &&
                               customer.getId().equals(equipment.customer.getId());
        
        // Length can be null, so handle carefully
        boolean lengthMatch = (length == null && equipment.length == null) ||
                             (length != null && length.equals(equipment.length));
        
        return brandMatch && modelMatch && lengthMatch && customerMatch;
    }

    @Override
    public int hashCode() {
        // If entity has ID, use it for hashCode (persisted entity)
        if (id != null) {
            return id.hashCode();
        }
        
        // For transient entities, use business key: brand + model + length + customer
        int result = brand != null ? brand.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + (customer != null && customer.getId() != null ? customer.getId().hashCode() : 0);
        return result;
    }
}
