// This Java code snippet defines a repository interface `WorkOrderRepository` that extends
// `JpaRepository` interface provided by Spring Data JPA.
package com.finetune.app.repository;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    /**
     * Find all open work orders for a customer.
     * Open work orders are those with status != "PICKED_UP".
     * Used for merging incoming ski items into existing orders.
     *
     * @param customer the customer to search for
     * @return list of open work orders
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer AND w.status != 'PICKED_UP' ORDER BY w.createdAt DESC")
    List<WorkOrder> findOpenWorkOrdersByCustomer(@Param("customer") Customer customer);

    /**
     * Find the most recent open work order for a customer.
     * Used to merge new ski items into the existing open order if available.
     *
     * @param customer the customer to search for
     * @return the most recent open work order, or empty if none exist
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer AND w.status != 'PICKED_UP' ORDER BY w.createdAt DESC LIMIT 1")
    Optional<WorkOrder> findMostRecentOpenWorkOrder(@Param("customer") Customer customer);
}
