package com.finetune.app.repository;

import com.finetune.app.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * EquipmentRepository provides data access for Equipment entities.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    /**
     * Find all equipment belonging to a specific customer.
     * 
     * @param customerId Customer ID
     * @return List of equipment items
     */
    @Query("SELECT e FROM Equipment e WHERE e.customer.id = :customerId ORDER BY e.id ASC")
    List<Equipment> findByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * Find all equipment linked to a specific work order.
     * 
     * @param workOrderId Work Order ID
     * @return List of equipment items
     */
    @Query("SELECT e FROM Equipment e WHERE e.workOrder.id = :workOrderId ORDER BY e.id ASC")
    List<Equipment> findByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
