package com.finetune.app.model.dto;

import com.finetune.app.model.Equipment;
import com.finetune.app.model.WorkOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for public work order creation response.
 * Returns work order ID, status, and equipment with associated boots.
 */
public class PublicWorkOrderCreationResponse {
    
    private Long workOrderId;
    private String status;
    private List<EquipmentOptionResponse> equipmentOptions = new ArrayList<>();

    // Error fields (for error responses)
    private Boolean error;
    private String message;

    public PublicWorkOrderCreationResponse() {}

    /**
     * Create a success response from a WorkOrder entity.
     */
    public static PublicWorkOrderCreationResponse fromWorkOrder(WorkOrder workOrder) {
        PublicWorkOrderCreationResponse response = new PublicWorkOrderCreationResponse();
        response.workOrderId = workOrder.getId();
        response.status = "created";
        response.error = false;
        
        // Build equipment options with associated boots
        for (Equipment equipment : workOrder.getEquipment()) {
            EquipmentOptionResponse equipmentOption = new EquipmentOptionResponse();
            equipmentOption.setSkiId(equipment.getId());
            equipmentOption.setSkiName(buildEquipmentName(equipment));
            
            // Add associated boot if present
            if (equipment.getBoot() != null) {
                AssociatedBootResponse boot = new AssociatedBootResponse(
                    equipment.getBoot().getId(),
                    buildBootName(equipment.getBoot())
                );
                equipmentOption.addAssociatedBoot(boot);
            }
            
            response.equipmentOptions.add(equipmentOption);
        }
        
        return response;
    }

    /**
     * Create an error response.
     */
    public static PublicWorkOrderCreationResponse error(String message) {
        PublicWorkOrderCreationResponse response = new PublicWorkOrderCreationResponse();
        response.error = true;
        response.message = message;
        response.status = "error";
        return response;
    }

    /**
     * Build a formatted name for equipment.
     */
    private static String buildEquipmentName(Equipment equipment) {
        StringBuilder name = new StringBuilder();
        
        if (equipment.getBrand() != null) {
            name.append(equipment.getBrand());
        }
        
        if (equipment.getModel() != null) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(equipment.getModel());
        }
        
        if (equipment.getLength() != null) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(equipment.getLength()).append("cm");
        }
        
        if (equipment.getServiceType() != null) {
            if (name.length() > 0) {
                name.append(" - ");
            }
            name.append(equipment.getServiceType());
        }
        
        return name.length() > 0 ? name.toString() : "Unnamed Equipment";
    }

    /**
     * Build a formatted name for a boot.
     */
    private static String buildBootName(com.finetune.app.model.Boot boot) {
        StringBuilder name = new StringBuilder();
        
        if (boot.getBrand() != null) {
            name.append(boot.getBrand());
        }
        
        if (boot.getModel() != null) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(boot.getModel());
        }
        
        if (boot.getBsl() != null) {
            if (name.length() > 0) {
                name.append(" (BSL: ");
            } else {
                name.append("BSL: ");
            }
            name.append(boot.getBsl()).append("mm");
            if (name.indexOf("(") >= 0) {
                name.append(")");
            }
        }
        
        return name.length() > 0 ? name.toString() : "Unnamed Boot";
    }

    // Getters and Setters
    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<EquipmentOptionResponse> getEquipmentOptions() {
        return equipmentOptions;
    }

    public void setEquipmentOptions(List<EquipmentOptionResponse> equipmentOptions) {
        this.equipmentOptions = equipmentOptions;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
