/**
 * WorkOrderService handles the business logic for creating and merging work orders.
 * 
 * Key responsibilities:
 * 1. Find or create customers based on email and phone
 * 2. Merge ski items into existing open work orders if available
 * 3. Create new work orders when no open orders exist
 * 4. Manage bidirectional relationships (Customer -> WorkOrder -> SkiItem)
 * 5. Track individual ski item status and calculate overall work order status
 * 
 * Merge Logic:
 * - When a customer submits new ski items, the service checks for open work orders
 * - If an open work order exists (status != PICKED_UP), new items are added to it
 * - If no open orders exist, a new work order is created
 * - The overall work order status is DONE only when ALL ski items are DONE
 * 
 * Notifications:
 * - New work orders trigger notifications to customers
 * - Merged items do NOT trigger notifications (customer was already notified)
 */
package com.finetune.app.service;

import com.finetune.app.model.dto.CreateWorkOrderRequest;
import com.finetune.app.model.dto.SkiItemRequest;
import com.finetune.app.model.entity.Customer;
import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.model.entity.SkiItem;
import com.finetune.app.repository.WorkOrderRepository;
import com.finetune.app.repository.CustomerRepository;
import com.finetune.app.repository.SkiItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final SkiItemRepository skiItemRepository;
    private final CustomerService customerService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            CustomerRepository customerRepository,
            SkiItemRepository skiItemRepository,
            CustomerService customerService) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.skiItemRepository = skiItemRepository;
        this.customerService = customerService;
    }

    /**
     * Creates or merges a work order based on the incoming request.
     * 
     * Process:
     * 1. Find or create a customer using email and phone
     * 2. Search for any open work orders for that customer
     * 3. If found, merge new ski items into the existing order
     * 4. If not found, create a new work order
     * 5. Save all changes (cascade handles persistence of related entities)
     * 
     * @param request the create work order request with customer and ski item details
     * @return the work order (new or merged) with all ski items
     */
    @Transactional
    public WorkOrder createOrMergeWorkOrder(CreateWorkOrderRequest request) {
        // Step 1: Find or create customer
        Customer customer = customerService.findOrCreateCustomer(
            request.getCustomerFirstName(),
            request.getCustomerLastName(),
            request.getEmail(),
            request.getPhone()
        );

        // Step 2: Find the most recent open work order for this customer
        Optional<WorkOrder> existingOpenOrder = 
            workOrderRepository.findMostRecentOpenWorkOrder(customer);

        WorkOrder workOrder;
        boolean isNewWorkOrder = false;

        if (existingOpenOrder.isPresent()) {
            // Merge case: Add new items to existing open work order
            workOrder = existingOpenOrder.get();
            // NOTE: No notification sent for merged items (customer already notified for original order)
        } else {
            // Create new work order case
            workOrder = new WorkOrder();
            workOrder.setStatus("RECEIVED");
            workOrder.setCreatedAt(LocalDateTime.now());
            customer.addWorkOrder(workOrder);
            isNewWorkOrder = true;
            // NOTE: Notification would be sent here for new work order
        }

        // Step 3: Add all incoming ski items to the work order (new or existing)
        if (request.getSkis() != null && !request.getSkis().isEmpty()) {
            for (SkiItemRequest skiReq : request.getSkis()) {
                SkiItem skiItem = new SkiItem(
                    skiReq.getSkiMake(),
                    skiReq.getSkiModel(),
                    skiReq.getServiceType()
                );
                skiItem.setStatus("PENDING"); // All new items start as PENDING
                workOrder.addSkiItem(skiItem);
            }
        }

        // Step 4: Update work order status based on all items (new + existing)
        // Status remains RECEIVED unless all items are DONE
        workOrder.updateStatusBasedOnItems();

        // Step 5: Persist the work order (cascade will save ski items)
        // If customer is new, the cascade from customerRepository.save will save everything
        customerRepository.save(customer);

        return workOrder;
    }

    /**
     * Updates the status of a specific ski item and recalculates the work order status.
     * 
     * Process:
     * 1. Find the ski item by ID
     * 2. Update its status to the new value
     * 3. Find the work order that contains this item
     * 4. Recalculate the work order's status based on ALL items
     * 5. Save the work order and return it
     * 
     * @param workOrderId the ID of the work order
     * @param skiItemId the ID of the ski item to update
     * @param newStatus the new status for the ski item (PENDING, IN_PROGRESS, DONE)
     * @return the updated work order
     */
    @Transactional
    public WorkOrder updateSkiItemStatus(Long workOrderId, Long skiItemId, String newStatus) {
        // Find the work order
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // Find the ski item within this work order
        SkiItem skiItem = workOrder.getSkiItems().stream()
            .filter(item -> item.getId().equals(skiItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ski item not found in work order: " + skiItemId));

        // Update the ski item's status
        skiItem.setStatus(newStatus);

        // Recalculate work order status based on all items
        workOrder.updateStatusBasedOnItems();

        // Save and return the work order
        return workOrderRepository.save(workOrder);
    }

    /**
     * Marks a work order as PICKED_UP (closed).
     * Also marks all ski items in the order as PICKED_UP.
     * Once picked up, no new items can be merged into this order.
     * New items for this customer will create a new work order.
     * 
     * @param workOrderId the ID of the work order to mark as picked up
     * @return the updated work order (with all items marked as PICKED_UP)
     */
    @Transactional
    public WorkOrder pickupWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // Mark the work order as PICKED_UP
        workOrder.setStatus("PICKED_UP");
        
        // Mark all ski items in this order as PICKED_UP
        if (workOrder.getSkiItems() != null && !workOrder.getSkiItems().isEmpty()) {
            for (SkiItem skiItem : workOrder.getSkiItems()) {
                skiItem.setStatus("PICKED_UP");
            }
        }
        
        return workOrderRepository.save(workOrder);
    }

    /**
     * Basic save method for persisting work orders.
     * 
     * @param order the work order to save
     * @return the saved work order
     */
    public WorkOrder save(WorkOrder order) {
        return workOrderRepository.save(order);
    }
}
