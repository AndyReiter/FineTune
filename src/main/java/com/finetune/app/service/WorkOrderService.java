/**
 * WorkOrderService handles the business logic for creating and merging work orders.
 * 
 * Key responsibilities:
 * 1. Find or create customers based on email and phone
 * 2. Merge ski items into existing active work orders if available
 * 3. Create new work orders when no active orders exist
 * 4. Manage bidirectional relationships (Customer -> WorkOrder -> SkiItem)
 * 5. Track individual ski item status and calculate overall work order status
 * 
 * Merge Logic:
 * - When a customer submits new ski items, the service checks for active work orders
 * - Active work orders are those with status "RECEIVED" or "IN_PROGRESS" 
 * - If an active work order exists, new items are added to it
 * - If no active orders exist, a new work order is created
 * - Completed or picked-up work orders are never reused; new work orders are created
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
import com.finetune.app.model.entity.Boot;
import com.finetune.app.model.enums.SkiItemStatus;
import com.finetune.app.model.enums.WorkOrderStatus;
import com.finetune.app.repository.WorkOrderRepository;
import com.finetune.app.repository.CustomerRepository;
import com.finetune.app.repository.SkiItemRepository;
import com.finetune.app.repository.BootRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final SkiItemRepository skiItemRepository;
    private final BootRepository bootRepository;
    private final CustomerService customerService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            CustomerRepository customerRepository,
            SkiItemRepository skiItemRepository,
            BootRepository bootRepository,
            CustomerService customerService) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.skiItemRepository = skiItemRepository;
        this.bootRepository = bootRepository;
        this.customerService = customerService;
    }

    /**
     * Helper method to convert height from feet and inches to total inches.
     * 
     * @param feet the feet component of height
     * @param inches the inches component of height  
     * @return total height in inches
     */
    private int convertToInches(int feet, int inches) {
        return (feet * 12) + inches;
    }

    /**
     * Creates or merges a work order based on the incoming request.
     * Prevents duplicate SkiItems by implementing intelligent merging logic.
     * 
     * Process:
     * 1. Find or create a customer using email and phone
     * 2. Search for any open work orders for that customer
     * 3. Save WorkOrder first (without SkiItems attached)
     * 4. Process each SkiItem with duplication prevention:
     *    - If status is PICKED_UP or COMPLETED: always create new SkiItem
     *    - If status is IN_PROGRESS or RECEIVED: merge only if exact match
     * 5. Handle boot linking without duplicate persistence
     * 6. Attach SkiItems to WorkOrder after checks
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

        // Ensure we are working with a managed Customer instance.
        // This avoids subtle merge/detached-entity behavior when we add new Boots.
        final Long customerId = customer.getId();
        customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Step 2: Find active work order for this customer (RECEIVED or IN_PROGRESS only)
        List<String> activeStatuses = Arrays.asList(
            WorkOrderStatus.RECEIVED.name(),
            WorkOrderStatus.IN_PROGRESS.name()
        );
        Optional<WorkOrder> existingActiveOrder = 
            workOrderRepository.findFirstByCustomerIdAndStatusIn(customer.getId(), activeStatuses);

        WorkOrder workOrder;
        boolean isNewWorkOrder = false;

        if (existingActiveOrder.isPresent()) {
            // Merge case: Add new items to existing active work order
            workOrder = existingActiveOrder.get();
            // Ensure the work order is attached to the current session
            workOrder = workOrderRepository.findById(workOrder.getId()).orElse(workOrder);
            // NOTE: No notification sent for merged items (customer already notified for original order)
        } else {
            // Create new work order case (no active work order exists)
            workOrder = new WorkOrder();
            // Initial status will be set by updateStatusBasedOnItems() after adding items
            workOrder.setCreatedAt(LocalDateTime.now());
            customer.addWorkOrder(workOrder);
            
            // Step 3a: Save the customer (and cascade to work order) BEFORE adding ski items
            // This ensures the WorkOrder has an ID and is properly managed by Hibernate
            customer = customerRepository.save(customer);
            
            // Get the persisted work order from the customer's work orders
            workOrder = customer.getWorkOrders().get(customer.getWorkOrders().size() - 1);
            
            isNewWorkOrder = true;
            // NOTE: Notification would be sent here for new work order
        }

        // Step 3b: For existing work orders, save first to ensure proper session management
        if (!isNewWorkOrder) {
            workOrder = workOrderRepository.save(workOrder);
        }

        // Step 4: Process all incoming ski items with duplication prevention
        if (request.getSkis() != null && !request.getSkis().isEmpty()) {
            // Track processed ski items within this request to prevent duplicates
            // (e.g., UI double-adds the same ski row into the payload)
            Set<String> processedSkiKeys = new HashSet<>();
            
            for (SkiItemRequest skiReq : request.getSkis()) {
                // Create a unique key for this ski item request
                String skiKey = createSkiItemKey(skiReq);
                
                if (processedSkiKeys.contains(skiKey)) {
                    // Skip duplicate ski items within the same request
                    continue;
                }
                
                processedSkiKeys.add(skiKey);
                processSkiItemWithDuplicationPrevention(customer, workOrder, skiReq);
            }
        }

        // Step 5: Recalculate work order status based on all items (item-driven logic)
        workOrder.updateStatusBasedOnItems();

        // Step 6: Save the work order with all its ski items
        workOrder = workOrderRepository.save(workOrder);

        return workOrder;
    }

    /**
     * Processes a ski item request with duplication prevention logic.
     * 
     * Logic:
        * 1. For MOUNT service type: resolve the Boot to link (existing boot preferred)
        * 2. Check existing SkiItems in the WorkOrder for an EXACT match:
        *    - serviceType
        *    - skiMake
        *    - skiModel
        *    - boot ID (MOUNT only)
        * 3. Merge (i.e., do nothing / reuse) ONLY when the WorkOrder is active
        *    (RECEIVED or IN_PROGRESS) AND the matching item is not PICKED_UP.
        * 4. If the matching item is PICKED_UP, or the WorkOrder is COMPLETED,
        *    always create a new SkiItem.
     * 
     * @param customer the customer
     * @param workOrder the work order to add/merge into
     * @param skiReq the ski item request
     */
    private void processSkiItemWithDuplicationPrevention(Customer customer, WorkOrder workOrder, SkiItemRequest skiReq) {
        Boot boot = null;
        
        // Step 1: Handle boot for mount service type
        if ("MOUNT".equals(skiReq.getServiceType())) {
            validateMountRequest(customer, skiReq);
            boot = handleBootForMount(customer, skiReq);
        }
        
        // Step 2: Check for existing matching SkiItems in the work order
        SkiItem existingMatch = findMatchingSkiItemInWorkOrder(workOrder, skiReq, boot);
        
        if (existingMatch != null && canMergeSkiItem(existingMatch)) {
            // Merge case: existing item found and can be merged
            // No action needed - item already exists and is mergeable
            return;
        }
        
        // Step 3: Create new SkiItem (no match found or cannot merge)
        createNewSkiItem(customer, workOrder, skiReq, boot);
    }

    /**
     * Handles boot lookup/creation for mount service type.
     * Links existing boot if found, creates new boot if needed.
     * 
     * @param customer the customer
     * @param skiReq the ski item request
     * @return the boot (existing or new)
     */
    private Boot handleBootForMount(Customer customer, SkiItemRequest skiReq) {
        Boot boot;
        
        if (skiReq.getBootId() != null) {
            // Use existing boot - no persistence needed
            boot = bootRepository.findById(skiReq.getBootId())
                .orElseThrow(() -> new IllegalArgumentException("Boot not found with ID: " + skiReq.getBootId()));
            
            // Ensure boot belongs to the same customer
            if (!boot.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Boot belongs to another customer and cannot be used");
            }
        } else {
            // Check if customer already has matching boot
            Boot existingBoot = customer.findMatchingBoot(
                skiReq.getBootBrand(),
                skiReq.getBootModel(),
                skiReq.getBsl()
            );
            
            if (existingBoot != null) {
                // Use existing boot - no persistence needed
                boot = existingBoot;
            } else {
                // Create new boot and add to customer
                boot = new Boot(
                    skiReq.getBootBrand(),
                    skiReq.getBootModel(),
                    skiReq.getBsl(),
                    skiReq.getHeightInches(),
                    skiReq.getWeight(),
                    skiReq.getAge(),
                    skiReq.getSkiAbilityLevel()
                );
                
                // Add to customer to maintain ownership (bidirectional link)
                customer.addBoot(boot);

                // IMPORTANT: Persist the Boot BEFORE attaching it to any SkiItem.
                // Otherwise Hibernate can throw:
                //   TransientPropertyValueException: SkiItem.boot references an unsaved transient Boot
                // We intentionally persist Boot via BootRepository and persist SkiItem via WorkOrder cascade.
                boot = bootRepository.save(boot);
            }
        }
        
        return boot;
    }

    /**
     * Finds an existing SkiItem in the work order that matches the incoming request exactly.
     * Match criteria: serviceType, skiMake, skiModel, boot (for mount services)
     * 
     * @param workOrder the work order to search
     * @param skiReq the ski item request
     * @param boot the boot (null for non-mount services)
     * @return matching SkiItem or null if no match
     */
    private SkiItem findMatchingSkiItemInWorkOrder(WorkOrder workOrder, SkiItemRequest skiReq, Boot boot) {
        return workOrder.getSkiItems().stream()
            .filter(existing -> skiItemsMatch(existing, skiReq, boot))
            .findFirst()
            .orElse(null);
    }

    /**
     * Determines if two ski items match exactly.
     * Match criteria: serviceType, skiMake, skiModel, boot (for mount services)
     * 
     * @param existing the existing SkiItem
     * @param skiReq the incoming request
     * @param boot the boot for mount services (null for others)
     * @return true if items match exactly
     */
    private boolean skiItemsMatch(SkiItem existing, SkiItemRequest skiReq, Boot boot) {
        // Basic matching: service type, make, model
        if (!existing.getServiceType().equals(skiReq.getServiceType()) ||
            !existing.getSkiMake().equals(skiReq.getSkiMake()) ||
            !existing.getSkiModel().equals(skiReq.getSkiModel())) {
            return false;
        }
        
        // For mount services, boot must also match
        if ("MOUNT".equals(skiReq.getServiceType())) {
            if (existing.getBoot() == null || boot == null) {
                return existing.getBoot() == null && boot == null;
            }
            return existing.getBoot().getId().equals(boot.getId());
        }
        
        return true;
    }

    /**
     * Determines if a SkiItem can be merged based on its status.
     * Rules:
     * - PICKED_UP or work order COMPLETED: never merge (always create new)
     * - IN_PROGRESS or work order RECEIVED: can merge
     * 
     * @param skiItem the existing ski item
     * @return true if item can be merged
     */
    private boolean canMergeSkiItem(SkiItem skiItem) {
        String itemStatus = skiItem.getStatus();
        String workOrderStatus = skiItem.getWorkOrder().getStatus();
        
        // Never merge if item was picked up or work order is completed
        if ("PICKED_UP".equals(itemStatus) || 
            WorkOrderStatus.COMPLETED.name().equals(workOrderStatus)) {
            return false;
        }
        
        // Can merge if work order is in active status and item is not yet picked up
        return Arrays.asList(
            WorkOrderStatus.RECEIVED.name(),
            WorkOrderStatus.IN_PROGRESS.name()
        ).contains(workOrderStatus);
    }

    /**
     * Creates a new SkiItem and adds it to the work order.
     * 
     * @param customer the customer
     * @param workOrder the work order
     * @param skiReq the ski item request
     * @param boot the boot (null for non-mount services)
     */
    private void createNewSkiItem(Customer customer, WorkOrder workOrder, SkiItemRequest skiReq, Boot boot) {
        SkiItem skiItem = new SkiItem(
            skiReq.getSkiMake(),
            skiReq.getSkiModel(),
            skiReq.getServiceType()
        );
        skiItem.setStatus(SkiItemStatus.PENDING.name()); // All new items start as PENDING
        
        // Handle mount-specific requirements
        if ("MOUNT".equals(skiReq.getServiceType())) {
            // Populate basic ski item data (binding info, condition)
            populateBasicSkiItemData(skiItem, skiReq);
            
            // Attach boot and populate profile data
            attachBootToSkiItem(skiItem, skiReq, boot);
            
            // Validate all required fields are present
            validateMountRequirements(skiItem);
            
            // Update customer profile with any new information
            updateCustomerProfileFromSkiItem(customer, skiItem);
        }
        
        // Add to work order
        workOrder.addSkiItem(skiItem);
    }

    /**
     * Creates a unique key for a ski item request to detect duplicates within the same request.
     * Key includes: serviceType, skiMake, skiModel, and bootId (for mount services).
     * 
     * @param skiReq the ski item request
     * @return unique key string
     */
    private String createSkiItemKey(SkiItemRequest skiReq) {
        StringBuilder key = new StringBuilder();
        key.append(skiReq.getServiceType()).append("|");
        key.append(skiReq.getSkiMake()).append("|");
        key.append(skiReq.getSkiModel()).append("|");
        
        // For mount services, include boot information to distinguish different boots
        if ("MOUNT".equals(skiReq.getServiceType())) {
            if (skiReq.getBootId() != null) {
                key.append("bootId:").append(skiReq.getBootId());
            } else {
                // For new boots, use boot brand/model/bsl as distinguisher
                key.append("boot:").append(skiReq.getBootBrand())
                   .append("|").append(skiReq.getBootModel())
                   .append("|").append(skiReq.getBsl());
            }
        }
        
        return key.toString();
    }

    /**
     * Attaches boot to ski item and populates profile data.
     * 
     * @param skiItem the ski item
     * @param skiReq the original request
     * @param boot the boot to attach
     */
    private void attachBootToSkiItem(SkiItem skiItem, SkiItemRequest skiReq, Boot boot) {
        // Attach boot to ski item.
        // IMPORTANT: Do NOT add the SkiItem to boot.getSkiItems() here.
        // Boot has cascade=ALL to SkiItem, and adding to that collection can introduce
        // an additional cascade-persist path. We want persistence for new SkiItems to
        // flow ONLY through WorkOrder -> SkiItem cascade.
        skiItem.setBoot(boot);
        
        // Populate SkiItem with profile data
        if (boot.getHeightInches() != null) {
            skiItem.setHeightInches(boot.getHeightInches());
        } else {
            skiItem.setHeightInches(skiReq.getHeightInches());
        }
        
        if (boot.getWeight() != null) {
            skiItem.setWeight(boot.getWeight());
        } else {
            skiItem.setWeight(skiReq.getWeight());
        }
        
        if (boot.getAge() != null) {
            skiItem.setAge(boot.getAge());
        } else {
            skiItem.setAge(skiReq.getAge());
        }
        
        if (boot.getAbilityLevel() != null) {
            skiItem.setSkiAbilityLevel(boot.getAbilityLevel());
        } else {
            skiItem.setSkiAbilityLevel(skiReq.getSkiAbilityLevel());
        }
    }

    /**
     * Updates the status of a specific ski item and recalculates the work order status.
     * Enforces business rules for valid item status transitions.
     * 
     * Valid item status transitions:
     * - PENDING → IN_PROGRESS
     * - IN_PROGRESS → DONE
     * - Any status → PICKED_UP (only via pickup workflow)
     * 
     * Process:
     * 1. Find and validate the ski item
     * 2. Validate the status transition
     * 3. Update the item status
     * 4. Recalculate work order status (item-driven)
     * 5. Save and return the work order
     * 
     * @param workOrderId the ID of the work order
     * @param skiItemId the ID of the ski item to update
     * @param newStatus the new status for the ski item
     * @return the updated work order
     * @throws IllegalArgumentException if validation fails
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

        // Validate the status transition
        validateItemStatusTransition(skiItem.getStatus(), newStatus);
        
        // Prevent manual setting of PICKED_UP (only via pickup workflow)
        if ("PICKED_UP".equals(newStatus)) {
            throw new IllegalArgumentException("PICKED_UP status can only be set via pickup workflow, not manually");
        }

        // Update the ski item's status
        skiItem.setStatus(newStatus);

        // Recalculate work order status based on all items (item-driven logic)
        workOrder.updateStatusBasedOnItems();

        // Save and return the work order
        return workOrderRepository.save(workOrder);
    }

    /**
     * Validates that an item status transition is allowed per business rules.
     * 
     * Valid transitions:
     * - PENDING → IN_PROGRESS
     * - IN_PROGRESS → DONE  
     * - Any status → PICKED_UP (only via pickup workflow, not manual)
     * 
     * @param currentStatus the current status of the item
     * @param newStatus the requested new status
     * @throws IllegalArgumentException if transition is invalid
     */
    private void validateItemStatusTransition(String currentStatus, String newStatus) {
        // Allow same status (no-op)
        if (currentStatus.equals(newStatus)) {
            return;
        }
        
        List<String> validStatuses = Arrays.asList("PENDING", "IN_PROGRESS", "DONE", "PICKED_UP");
        if (!validStatuses.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Valid statuses: " + validStatuses);
        }
        
        // Define valid transitions
        boolean validTransition = false;
        
        switch (currentStatus) {
            case "PENDING":
                validTransition = "IN_PROGRESS".equals(newStatus);
                break;
            case "IN_PROGRESS":
                validTransition = "DONE".equals(newStatus) || "PENDING".equals(newStatus);
                break;
            case "DONE":
                validTransition = "IN_PROGRESS".equals(newStatus);
                break;
            case "PICKED_UP":
                // Once picked up, cannot change status
                validTransition = false;
                break;
            default:
                validTransition = false;
        }
        
        if (!validTransition) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition: %s → %s. Items must progress: PENDING → IN_PROGRESS → DONE", 
                    currentStatus, newStatus));
        }
    }
    
    /**
     * Centralized method to calculate work order status from item statuses.
     * This enforces the item-driven status logic and should be used whenever
     * item statuses change or items are added/removed.
     * 
     * @param workOrder the work order to recalculate status for
     */
    public void recalculateWorkOrderStatus(WorkOrder workOrder) {
        workOrder.updateStatusBasedOnItems();
    }
    /**
     * ATOMIC PICKUP OPERATION: Marks all items as PICKED_UP and work order as COMPLETED.
     * 
     * This is the ONLY way to set items to PICKED_UP status. Pickup is treated as a single
     * business event where the customer takes possession of ALL items at once.
     * 
     * VALIDATION RULES:
     * - Work order must exist
     * - Work order must be in READY_FOR_PICKUP status
     * - ALL items must be in DONE status
     * - Partial pickup is NOT allowed
     * 
     * ATOMIC OPERATION:
     * - Updates ALL ski items → PICKED_UP
     * - Updates work order → COMPLETED
     * - Either ALL changes succeed or ALL changes fail
     * 
     * @param workOrderId the ID of the work order to pickup
     * @return the updated work order with COMPLETED status
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public WorkOrder pickupWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // VALIDATION: Work order must be awaiting pickup (customer notified)
        if (!"AWAITING_PICKUP".equals(workOrder.getStatus())) {
            throw new IllegalArgumentException(
                String.format("Cannot pickup work order in status: %s. Must be AWAITING_PICKUP (customer notified).", 
                    workOrder.getStatus()));
        }

        // VALIDATION: ALL items must be DONE (no partial pickups allowed)
        List<SkiItem> nonDoneItems = workOrder.getSkiItems().stream()
            .filter(item -> !"DONE".equals(item.getStatus()))
            .collect(Collectors.toList());
        
        if (!nonDoneItems.isEmpty()) {
            String itemStatuses = nonDoneItems.stream()
                .map(item -> String.format("%s %s (%s): %s", 
                    item.getSkiMake(), item.getSkiModel(), item.getServiceType(), item.getStatus()))
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                String.format("Cannot pickup work order: %d items are not DONE: %s. All items must be DONE before pickup.",
                    nonDoneItems.size(), itemStatuses));
        }

        // ATOMIC OPERATION: Update ALL items to PICKED_UP
        workOrder.getSkiItems().forEach(item -> {
            item.setStatus("PICKED_UP");
        });
        
        // Set work order status to COMPLETED
        workOrder.setStatus(WorkOrderStatus.COMPLETED.name());
        
        // Save and return (transactional - either all succeed or all fail)
        return workOrderRepository.save(workOrder);
    }

    /**
     * Notify customer that work order is ready for pickup.
     * Transitions work order from READY_FOR_PICKUP to AWAITING_PICKUP.
     * 
     * VALIDATION RULES:
     * - Work order must exist
     * - Work order must be in READY_FOR_PICKUP status
     * - ALL items must be in DONE status
     * 
     * BUSINESS LOGIC:
     * - Updates work order status to AWAITING_PICKUP
     * - Preserves AWAITING_PICKUP status until actual pickup occurs
     * - Future: Can be extended to send actual notifications (SMS, email)
     * 
     * @param workOrderId the ID of the work order to notify customer about
     * @return the updated work order with AWAITING_PICKUP status
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public WorkOrder notifyCustomer(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // VALIDATION: Work order must be ready for pickup
        if (!"READY_FOR_PICKUP".equals(workOrder.getStatus())) {
            throw new IllegalArgumentException(
                String.format("Cannot notify customer for work order in status: %s. Must be READY_FOR_PICKUP.", 
                    workOrder.getStatus()));
        }

        // VALIDATION: ALL items must be DONE (double-check business rules)
        List<SkiItem> nonDoneItems = workOrder.getSkiItems().stream()
            .filter(item -> !"DONE".equals(item.getStatus()))
            .collect(Collectors.toList());
        
        if (!nonDoneItems.isEmpty()) {
            String itemStatuses = nonDoneItems.stream()
                .map(item -> String.format("%s %s (%s): %s", 
                    item.getSkiMake(), item.getSkiModel(), item.getServiceType(), item.getStatus()))
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                String.format("Cannot notify customer: %d items are not DONE: %s. All items must be DONE before notification.",
                    nonDoneItems.size(), itemStatuses));
        }

        // TRANSITION: Update work order to AWAITING_PICKUP
        workOrder.setStatus(WorkOrderStatus.AWAITING_PICKUP.name());
        
        // TODO: Future enhancement - send actual notification (SMS, email, push)
        // Example: notificationService.sendPickupReadyNotification(workOrder.getCustomer());
        
        // Save and return
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

    /**
     * Validates that all required fields are present for MOUNT service type.
     * 
     * @param skiReq the ski item request to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateMountRequirements(SkiItemRequest skiReq) {
        if (skiReq.getBindingBrand() == null || skiReq.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (skiReq.getBindingModel() == null || skiReq.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        if (skiReq.getBootBrand() == null || skiReq.getBootBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot brand is required for MOUNT service");
        }
        if (skiReq.getBootModel() == null || skiReq.getBootModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot model is required for MOUNT service");
        }
        if (skiReq.getBsl() == null) {
            throw new IllegalArgumentException("BSL (Boot Sole Length) is required for MOUNT service");
        }
        if (skiReq.getHeightInches() == null) {
            throw new IllegalArgumentException("Height is required for MOUNT service");
        }
        if (skiReq.getWeight() == null) {
            throw new IllegalArgumentException("Weight is required for MOUNT service");
        }
        if (skiReq.getAge() == null) {
            throw new IllegalArgumentException("Age is required for MOUNT service");
        }
        if (skiReq.getSkiAbilityLevel() == null) {
            throw new IllegalArgumentException("Ski ability level is required for MOUNT service");
        }
        if (skiReq.getCondition() == null) {
            throw new IllegalArgumentException("Condition is required for MOUNT service");
        }
    }

    /**
     * Validates that all required fields are present for MOUNT service type on a SkiItem.
     * 
     * @param skiItem the ski item to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateMountRequirements(SkiItem skiItem) {
        if (skiItem.getBindingBrand() == null || skiItem.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (skiItem.getBindingModel() == null || skiItem.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        if (skiItem.getBoot() == null) {
            throw new IllegalArgumentException("Boot information is required for MOUNT service");
        }
        Boot boot = skiItem.getBoot();
        if (boot.getBrand() == null || boot.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot brand is required for MOUNT service");
        }
        if (boot.getModel() == null || boot.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot model is required for MOUNT service");
        }
        if (boot.getBsl() == null) {
            throw new IllegalArgumentException("BSL (Boot Sole Length) is required for MOUNT service");
        }
        if (skiItem.getHeightInches() == null) {
            throw new IllegalArgumentException("Height is required for MOUNT service");
        }
        if (skiItem.getWeight() == null) {
            throw new IllegalArgumentException("Weight is required for MOUNT service");
        }
        if (skiItem.getAge() == null) {
            throw new IllegalArgumentException("Age is required for MOUNT service");
        }
        if (skiItem.getSkiAbilityLevel() == null) {
            throw new IllegalArgumentException("Ski ability level is required for MOUNT service");
        }
        if (skiItem.getCondition() == null) {
            throw new IllegalArgumentException("Condition is required for MOUNT service");
        }
    }

    /**
     * Auto-fills missing mount fields from customer's skier profile.
     * Only fills skier profile fields (height, weight, ability) - NOT boot information.
     * Boot attachment is handled separately in attachBootToSkiItem method.
     * Only fills fields that are null in the ski item but have values in customer profile.
     * Does NOT auto-fill binding brand/model as those are ski-specific.
     * 
     * @param customer the customer with potential profile data
     * @param skiItem the ski item to auto-fill
     * @param skiReq the original request (for reference only)
     */
    private void autoFillFromCustomerProfile(Customer customer, SkiItem skiItem, SkiItemRequest skiReq) {
        // Auto-fill skier profile fields from customer if missing in ski item
        if (skiItem.getHeightInches() == null && customer.getHeightInches() != null) {
            skiItem.setHeightInches(customer.getHeightInches());
        }
        if (skiItem.getWeight() == null && customer.getWeight() != null) {
            skiItem.setWeight(customer.getWeight());
        }
        if (skiItem.getSkiAbilityLevel() == null && customer.getSkiAbilityLevel() != null) {
            skiItem.setSkiAbilityLevel(customer.getSkiAbilityLevel());
        }
        
        // NOTE: Boot assignment is handled separately in attachBootToSkiItem method.
        // This method only handles skier profile auto-fill to avoid conflicts.
    }

    /**
     * Validates mount request requirements.
     * 
     * @param customer the requesting customer
     * @param skiReq the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateMountRequest(Customer customer, SkiItemRequest skiReq) {
        // Validate binding information
        if (skiReq.getBindingBrand() == null || skiReq.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (skiReq.getBindingModel() == null || skiReq.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        
        // Boot validation: either bootId OR (bootBrand + bootModel + bsl) required
        boolean hasBootId = skiReq.getBootId() != null;
        boolean hasBootInfo = skiReq.getBootBrand() != null && 
                             skiReq.getBootModel() != null && 
                             skiReq.getBsl() != null;
        
        if (!hasBootId && !hasBootInfo) {
            throw new IllegalArgumentException("Either bootId or (bootBrand + bootModel + bsl) is required for MOUNT service");
        }
        
        // If creating new boot, additional fields are required
        if (!hasBootId) {
            if (skiReq.getHeightInches() == null) {
                throw new IllegalArgumentException("heightInches is required when creating new boot for MOUNT service");
            }
            if (skiReq.getWeight() == null) {
                throw new IllegalArgumentException("weight is required when creating new boot for MOUNT service");
            }
            if (skiReq.getAge() == null) {
                throw new IllegalArgumentException("age is required when creating new boot for MOUNT service");
            }
            if (skiReq.getSkiAbilityLevel() == null) {
                throw new IllegalArgumentException("abilityLevel is required when creating new boot for MOUNT service"); 
            }
        }
    }

    /**
     * DEPRECATED: Use handleBootForMount and attachBootToSkiItem instead.
     * This method is kept for backward compatibility but should not be used
     * in the new duplication prevention logic.
     * 
     * @deprecated Use handleBootForMount and attachBootToSkiItem instead
     */
    @Deprecated
    private void attachBootAndPopulateProfile(Customer customer, SkiItem skiItem, SkiItemRequest skiReq) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use handleBootForMount and attachBootToSkiItem instead."
        );
    }

    /**
     * Populates ski item with basic mount-specific data from request.
     * Only handles binding information and condition - not boot profile data.
     * Boot profile data is handled separately in attachBootAndPopulateProfile.
     * 
     * @param skiItem the ski item to populate
     * @param skiReq the request containing the data
     */
    private void populateBasicSkiItemData(SkiItem skiItem, SkiItemRequest skiReq) {
        // Set binding information (always from request)
        skiItem.setBindingBrand(skiReq.getBindingBrand());
        skiItem.setBindingModel(skiReq.getBindingModel());
        
        // Set condition (always from request)
        skiItem.setCondition(skiReq.getCondition());
        
        // Boot profile data (height, weight, age, ability) is handled in attachBootAndPopulateProfile
    }
    
    /**
     * Finds an existing boot with matching brand, model, and BSL, or creates a new one.
     * 
     * @param customer the customer to search boots for
     * @param brand the boot brand
     * @param model the boot model  
     * @param bsl the boot sole length
     * @return the found or created Boot entity
     */
    private Boot findOrCreateBoot(Customer customer, String brand, String model, Integer bsl) {
        // Try to find existing matching boot
        Boot existingBoot = customer.findMatchingBoot(brand, model, bsl);
        if (existingBoot != null) {
            return existingBoot;
        }
        
        // Create new boot if no match found
        Boot newBoot = new Boot(brand, model, bsl);
        customer.addBoot(newBoot);
        return newBoot;
    }

    /**
     * Saves skier profile data from ski item to customer for future auto-population.
     * Only overwrites customer data if new values are provided.
     * Boot information is automatically saved through the Boot entity relationship.
     * 
     * @param customer the customer to update
     * @param skiItem the ski item containing skier data
     */
    /**
     * Updates customer profile with skier information from SkiItem when creating new boots.
     * Only updates customer profile if the SkiItem has more recent or complete information.
     * 
     * @param customer the customer to update
     * @param skiItem the ski item containing skier data
     */
    private void updateCustomerProfileFromSkiItem(Customer customer, SkiItem skiItem) {
        // Update customer profile with skier information (for profile consistency)
        if (skiItem.getHeightInches() != null && customer.getHeightInches() == null) {
            customer.setHeightInches(skiItem.getHeightInches());
        }
        if (skiItem.getWeight() != null && customer.getWeight() == null) {
            customer.setWeight(skiItem.getWeight());
        }
        if (skiItem.getSkiAbilityLevel() != null && customer.getSkiAbilityLevel() == null) {
            customer.setSkiAbilityLevel(skiItem.getSkiAbilityLevel());
        }
        // Note: Age is typically not updated on customer profile as it's boot-specific
        // Boot information is now managed through the Boot entity relationship
    }

    // === STAFF DASHBOARD METHODS ===

    /**
     * Get all work orders for staff dashboard, ordered by creation date (oldest first).
     * 
     * @return list of all work orders
     */
    public List<WorkOrder> getAllWorkOrdersForStaff() {
        return workOrderRepository.findAllOrderByCreatedAtAsc();
    }

    /**
     * Get work orders filtered by status for staff dashboard.
     * 
     * @param status the status to filter by
     * @return list of work orders with the specified status
     */
    public List<WorkOrder> getWorkOrdersByStatus(String status) {
        return workOrderRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    /**
     * DEPRECATED: Direct work order status updates are not allowed.
     * Work order status MUST be derived from item statuses to maintain data consistency.
     * 
     * Use updateSkiItemStatus() to change individual item statuses, which will
     * automatically recalculate the work order status using item-driven logic.
     * 
     * @param workOrderId the work order ID
     * @param newStatus the new status (will be rejected)
     * @throws UnsupportedOperationException always - direct status updates not allowed
     */
    @Transactional
    public WorkOrder updateWorkOrderStatus(Long workOrderId, String newStatus) {
        throw new UnsupportedOperationException(
            "Direct work order status updates are not allowed. " +
            "Work order status must be derived from item statuses. " +
            "Use updateSkiItemStatus() to change item statuses, which will automatically " +
            "recalculate the work order status."
        );
    }

    // === MOUNT WORKFLOW HELPER METHODS ===

    /**
     * Finds customer by firstName, lastName, and phone number.
     * Used for mount workflow to look up existing customer and their boots.
     * 
     * @param firstName the customer's first name
     * @param lastName the customer's last name  
     * @param phone the customer's phone number
     * @return the customer if found
     * @throws IllegalArgumentException if customer not found
     */
    public Customer findCustomerForMountWorkflow(String firstName, String lastName, String phone) {
        // Find customers by phone number
        List<Customer> customersByPhone = customerRepository.findByEmailOrPhone("", phone);
        
        // Filter by first name and last name
        Optional<Customer> matchingCustomer = customersByPhone.stream()
            .filter(customer -> 
                firstName.equalsIgnoreCase(customer.getFirstName()) &&
                lastName.equalsIgnoreCase(customer.getLastName()))
            .findFirst();
            
        return matchingCustomer.orElseThrow(() -> 
            new IllegalArgumentException("Customer not found with name: " + firstName + " " + lastName + " and phone: " + phone));
    }

    /**
     * Gets all boots for a customer.
     * Used for mount workflow modal to display available boots.
     * 
     * @param customer the customer
     * @return list of boots (may be empty)
     */
    public List<Boot> getBootsForCustomer(Customer customer) {
        return customer.getBoots();
    }
}
