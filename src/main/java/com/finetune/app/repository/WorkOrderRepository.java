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

    /**
     * Find all work orders by status, ordered by creation date (oldest first).
     * Used for staff dashboard filtering.
     *
     * @param status the status to filter by
     * @return list of work orders with the specified status
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.status = :status ORDER BY w.createdAt ASC")
    List<WorkOrder> findByStatusOrderByCreatedAtAsc(@Param("status") String status);

    /**
     * Find all work orders with any of the specified statuses, ordered by creation date (oldest first).
     * Used for staff dashboard filtering when multiple statuses should be shown together.
     *
     * @param statuses the list of statuses to filter by
     * @return list of work orders with any of the specified statuses
     */
    @Query("SELECT w FROM WorkOrder w WHERE w.status IN :statuses ORDER BY w.createdAt ASC")
    List<WorkOrder> findByStatusInOrderByCreatedAtAsc(@Param("statuses") List<String> statuses);

    /**
     * Find all work orders ordered by creation date (oldest first).
     * Used for staff dashboard.
     *
     * @return list of all work orders ordered by creation date
     */
    @Query("SELECT w FROM WorkOrder w ORDER BY w.createdAt ASC")
    List<WorkOrder> findAllOrderByCreatedAtAsc();

    /**
     * Find the first work order for a specific customer with any of the given statuses.
     * Used for merging logic to find active work orders (RECEIVED or IN_PROGRESS).
     *
     * @param customerId the ID of the customer
     * @param statuses the list of statuses to match
     * @return the first work order matching criteria, or empty if none exist
     */
    Optional<WorkOrder> findFirstByCustomerIdAndStatusIn(Long customerId, List<String> statuses);
}
