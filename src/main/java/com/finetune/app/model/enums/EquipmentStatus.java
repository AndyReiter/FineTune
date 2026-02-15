package com.finetune.app.model.enums;

/**
 * Enum defining valid statuses for equipment items within a work order.
 * These statuses drive the overall work order status computation.
 * 
 * Status progression:
 * PENDING → IN_PROGRESS → DONE → PICKED_UP
 * 
 * Business rules:
 * - All new items start as PENDING
 * - Staff can manually set IN_PROGRESS and DONE
 * - PICKED_UP is only set when entire work order is picked up
 */
public enum EquipmentStatus {
    /**
     * Initial status for new equipment items.
     * Indicates work has not started yet.
     */
    PENDING,
    
    /**
     * Item is currently being worked on by staff.
     * Manual status set by technicians.
     */
    IN_PROGRESS,
    
    /**
     * Work on item is completed and ready for pickup.
     * Manual status set by technicians when work is finished.
     */
    DONE,
    
    /**
     * Item has been picked up by customer.
     * This status is only set when the entire work order is marked as picked up.
     * Cannot be set manually for individual items.
     */
    PICKED_UP
}
