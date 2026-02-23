/**
 * WorkOrderService handles the business logic for creating and merging work orders.
 * 
 * Key responsibilities:
 * 1. Find or create customers based on email and phone
 * 2. Merge equipment items into existing active work orders if available
 * 3. Create new work orders when no active orders exist
 * 4. Manage bidirectional relationships (Customer -> WorkOrder -> Equipment)
 * 5. Track individual equipment item status and calculate overall work order status
 * 
 * Merge Logic:
 * - When a customer submits new equipment items, the service checks for active work orders
 * - Active work orders are those with status "RECEIVED" or "IN_PROGRESS" 
 * - If an active work order exists, new items are added to it
 * - If no active orders exist, a new work order is created
 * - Completed or picked-up work orders are never reused; new work orders are created
 * - The overall work order status is DONE only when ALL equipment items are DONE
 * 
 * Notifications:
 * - New work orders trigger notifications to customers
 * - Merged items do NOT trigger notifications (customer was already notified)
 */
package com.finetune.app.service;

import com.finetune.app.model.dto.CreateWorkOrderRequest;
import com.finetune.app.model.dto.EquipmentItemRequest;
import com.finetune.app.model.dto.EquipmentRequest;
import com.finetune.app.model.Customer;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.model.Equipment;
import com.finetune.app.model.Boot;
import com.finetune.app.model.enums.EquipmentStatus;
import com.finetune.app.model.enums.WorkOrderStatus;
import com.finetune.app.exception.DailyLimitExceededException;
import com.finetune.app.repository.sql.WorkOrderSqlRepository;
import com.finetune.app.repository.sql.CustomerSqlRepository;
import com.finetune.app.repository.sql.EquipmentSqlRepository;
import com.finetune.app.repository.sql.BootSqlRepository;

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

    private final WorkOrderSqlRepository workOrderRepository;
    private final CustomerSqlRepository customerRepository;
    private final EquipmentSqlRepository equipmentRepository;
    private final BootSqlRepository bootRepository;
    private final CustomerService customerService;
    private final StaffSettingsService staffSettingsService;

    public WorkOrderService(
            WorkOrderSqlRepository workOrderRepository,
            CustomerSqlRepository customerRepository,
            EquipmentSqlRepository equipmentRepository,
            BootSqlRepository bootRepository,
            CustomerService customerService,
            StaffSettingsService staffSettingsService) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.equipmentRepository = equipmentRepository;
        this.bootRepository = bootRepository;
        this.customerService = customerService;
        this.staffSettingsService = staffSettingsService;
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
     * Checks if a customer has exceeded their daily work order limit.
     * This check only applies to customer-created work orders.
     * 
     * @param customer the customer to check
     * @throws DailyLimitExceededException if the customer has reached or exceeded their daily limit
     */
    private void checkDailyLimit(Customer customer) {
        // Get the max limit from settings
        int maxLimit = staffSettingsService.getMaxCustomerWorkOrdersPerDay();
        
        // Count customer-created work orders in the last 24 hours
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        // TODO: Implement countCustomerCreatedWorkOrdersSince in WorkOrderSqlRepository
        long count = 0; // workOrderRepository.countCustomerCreatedWorkOrdersSince(customer.getId(), twentyFourHoursAgo);
        
        // Check if limit is exceeded
        if (count >= maxLimit) {
            throw new DailyLimitExceededException(maxLimit);
        }
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
        return createOrMergeWorkOrder(request, false);
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
     * @param customerCreated true if this is a customer-created work order (for daily limit enforcement)
     * @return the work order (new or merged) with all ski items
     */
    @Transactional
    public WorkOrder createOrMergeWorkOrder(CreateWorkOrderRequest request, boolean customerCreated) {
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

        // Step 2: Check for duplicate active work order with matching equipment
        // Use the first ski item to detect if customer already has an active work order with this equipment
        WorkOrder workOrder = null;
        boolean isNewWorkOrder = false;
        
        if (request.getEquipment() != null && !request.getEquipment().isEmpty()) {
            EquipmentItemRequest firstItem = request.getEquipment().get(0);
            
            // For mount services, resolve boot first to check for duplicates
            Boot bootForDuplicateCheck = null;
            if ("MOUNT".equals(firstItem.getServiceType())) {
                if (firstItem.getBootId() != null) {
                    bootForDuplicateCheck = bootRepository.findById(firstItem.getBootId()).orElse(null);
                } else {
                    // Check if customer has matching boot
                    bootForDuplicateCheck = customer.findMatchingBoot(
                        firstItem.getBootBrand(),
                        firstItem.getBootModel(),
                        firstItem.getBsl()
                    );
                }
            }
            
            // Check for duplicate active work order
            Optional<WorkOrder> duplicateWorkOrder = findDuplicateActiveWorkOrder(customer, firstItem, bootForDuplicateCheck);
            if (duplicateWorkOrder.isPresent()) {
                workOrder = duplicateWorkOrder.get();
                // Ensure the work order is attached to the current session
                workOrder = workOrderRepository.findByIdWithEquipment(workOrder.getId()).orElse(workOrder);
                // Duplicate found - will merge into this work order
            }
        }
        
        // Step 3: If no duplicate found, find or create work order using existing logic
        if (workOrder == null) {
            List<String> activeStatuses = Arrays.asList(
                WorkOrderStatus.RECEIVED.name(),
                WorkOrderStatus.IN_PROGRESS.name()
            );
            // TODO: Implement findFirstByCustomerIdAndStatusIn in WorkOrderSqlRepository
            Optional<WorkOrder> existingActiveOrder = Optional.empty(); // workOrderRepository.findFirstByCustomerIdAndStatusIn(customer.getId(), activeStatuses);

            if (existingActiveOrder.isPresent()) {
                workOrder = existingActiveOrder.get();
                // TODO: Implement findByIdWithEquipment in WorkOrderSqlRepository if needed
            } else {
                if (customerCreated) {
                    checkDailyLimit(customer);
                }
                workOrder = new WorkOrder();
                if (customerCreated) {
                    workOrder.setStatus(WorkOrderStatus.CUSTOMER_SUBMITTED.name());
                }
                workOrder.setCreatedAt(LocalDateTime.now());
                workOrder.setPromisedBy(request.getPromisedBy());
                workOrder.setCustomerCreated(customerCreated);
                customer.addWorkOrder(workOrder);
                // TODO: Implement save logic for customer and workOrder in SqlRepositories
                // customer = customerRepository.save(customer);
                // workOrder = ...
                isNewWorkOrder = true;
            }
        }

        // Step 3b: For existing work orders, save first to ensure proper session management
        // TODO: Implement save logic for workOrder in SqlRepository if needed

        // Step 4: Process all incoming equipment items with duplication prevention
        if (request.getEquipment() != null && !request.getEquipment().isEmpty()) {
            // Track processed equipment items within this request to prevent duplicates
            // (e.g., UI double-adds the same row into the payload)
            Set<String> processedItemKeys = new HashSet<>();
            
            for (EquipmentItemRequest equipReq : request.getEquipment()) {
                // Create a unique key for this equipment item request
                String itemKey = createEquipmentItemKey(equipReq);
                
                if (processedItemKeys.contains(itemKey)) {
                    // Skip duplicate equipment items within the same request
                    continue;
                }
                
                processedItemKeys.add(itemKey);
                processEquipmentItemWithDuplicationPrevention(customer, workOrder, equipReq);
            }
        }

        // Step 5: Recalculate work order status based on all items (item-driven logic)
        updateWorkOrderStatusAndCompletedDate(workOrder);

        // Step 5.5: Update equipment service history for staff-created work orders
        if (!customerCreated && workOrder.getEquipment() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            workOrder.getEquipment().forEach(equipment -> {
                equipment.setLastServicedDate(today);
                equipment.setLastServiceType(equipment.getServiceType());
            });
        }

        // Step 6: Save the work order with all its ski items
        // TODO: Implement save logic for workOrder in SqlRepository if needed

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
    private void processEquipmentItemWithDuplicationPrevention(Customer customer, WorkOrder workOrder, EquipmentItemRequest equipReq) {
        // Validate equipment selection (equipmentId vs newEquipment)
        validateEquipmentSelection(equipReq);
        
        Boot boot = null;
        
        // Step 1: If equipmentId provided, use existing equipment
        if (equipReq.getEquipmentId() != null) {
            processExistingEquipment(customer, workOrder, equipReq);
            return;
        }
        
        // Step 2: Handle boot for mount service type (for new equipment)
        if ("MOUNT".equals(equipReq.getServiceType())) {
            validateMountRequest(customer, equipReq);
            boot = handleBootForMount(customer, equipReq);
        }
        
        // Step 3: Check for existing matching Equipment in the work order
        Equipment existingMatch = findMatchingEquipmentInWorkOrder(workOrder, equipReq, boot);
        
        if (existingMatch != null && canMergeEquipment(existingMatch)) {
            // Merge case: existing item found and can be merged
            // No action needed - item already exists and is mergeable
            return;
        }
        
        // Step 4: Create new Equipment (no match found or cannot merge)
        createNewEquipment(customer, workOrder, equipReq, boot);
    }

    /**
     * Processes an existing equipment item by ID.
     * Validates ownership and attaches to work order.
     * 
     * @param customer the customer
     * @param workOrder the work order
     * @param equipReq the equipment item request with equipmentId
     * @throws IllegalArgumentException if equipment not found or doesn't belong to customer
     */
    private void processExistingEquipment(Customer customer, WorkOrder workOrder, EquipmentItemRequest equipReq) {
        // Look up existing equipment
        Equipment equipment = equipmentRepository.findById(equipReq.getEquipmentId())
            .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipReq.getEquipmentId()));
        
        // Verify equipment belongs to this customer
        if (!equipment.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Equipment belongs to another customer and cannot be used");
        }
        
        // Set service type from request
        equipment.setServiceType(equipReq.getServiceType());
        equipment.setStatus(EquipmentStatus.PENDING.name()); // Reset status for new work order
        
        // Handle boot for MOUNT services
        if ("MOUNT".equals(equipReq.getServiceType())) {
            validateMountRequest(customer, equipReq);
            Boot boot = handleBootForMount(customer, equipReq);
            
            // Attach boot to equipment and update binding info
            equipment.setBoot(boot);
            equipment.setBindingBrand(equipReq.getBindingBrand());
            equipment.setBindingModel(equipReq.getBindingModel());
        }
        
        // Attach to work order (prevent duplicates)
        if (!workOrder.getEquipment().contains(equipment)) {
            workOrder.addEquipment(equipment);
        }
        
        // Save equipment changes (including work_order_id relationship)
        equipmentRepository.save(equipment);
    }

    /**
     * Handles boot lookup/creation for mount service type.
     * Links existing boot if found, creates new boot if needed.
     * 
     * @param customer the customer
     * @param equipReq the equipment item request
     * @return the boot (existing or new)
     */
    private Boot handleBootForMount(Customer customer, EquipmentItemRequest equipReq) {
        Boot boot;
        
        if (equipReq.getBootId() != null) {
            // Use existing boot - no persistence needed
            boot = bootRepository.findById(equipReq.getBootId())
                .orElseThrow(() -> new IllegalArgumentException("Boot not found with ID: " + equipReq.getBootId()));
            
            // Ensure boot belongs to the same customer
            if (!boot.getCustomer().getId().equals(customer.getId())) {
                throw new IllegalArgumentException("Boot belongs to another customer and cannot be used");
            }
        } else {
            // Check if customer already has matching boot
            Boot existingBoot = customer.findMatchingBoot(
                equipReq.getBootBrand(),
                equipReq.getBootModel(),
                equipReq.getBsl()
            );
            
            if (existingBoot != null) {
                // Use existing boot - no persistence needed
                boot = existingBoot;
            } else {
                // Create new boot and add to customer
                boot = new Boot(
                    equipReq.getBootBrand(),
                    equipReq.getBootModel(),
                    equipReq.getBsl(),
                    equipReq.getHeightInches(),
                    equipReq.getWeight(),
                    equipReq.getAge(),
                    equipReq.getSkiAbilityLevel()
                );
                
                // Add to customer to maintain ownership (bidirectional link)
                customer.addBoot(boot);

                // IMPORTANT: Persist the Boot BEFORE attaching it to any Equipment.
                // Otherwise Hibernate can throw:
                //   TransientPropertyValueException: Equipment.boot references an unsaved transient Boot
                // We intentionally persist Boot via BootRepository and persist Equipment via WorkOrder cascade.
                bootRepository.save(boot);
            }
        }
        
        return boot;
    }

    /**
     * Finds an existing Equipment in the work order that matches the incoming request exactly.
     * Match criteria: serviceType, brand, model, boot (for mount services)
     * 
     * @param workOrder the work order to search
     * @param equipReq the equipment item request
     * @param boot the boot (null for non-mount services)
     * @return matching Equipment or null if no match
     */
    private Equipment findMatchingEquipmentInWorkOrder(WorkOrder workOrder, EquipmentItemRequest equipReq, Boot boot) {
        return workOrder.getEquipment().stream()
            .filter(existing -> equipmentMatches(existing, equipReq, boot))
            .findFirst()
            .orElse(null);
    }

    /**
     * Determines if two equipment items match exactly.
     * Match criteria: serviceType, brand, model, boot (for mount services)
     * 
     * @param existing the existing Equipment
     * @param equipReq the incoming request
     * @param boot the boot for mount services (null for others)
     * @return true if items match exactly
     */
    private boolean equipmentMatches(Equipment existing, EquipmentItemRequest equipReq, Boot boot) {
        // Get brand and model from appropriate source
        String brand = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getBrand() 
            : equipReq.getSkiMake();
        String model = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getModel() 
            : equipReq.getSkiModel();
        
        // Basic matching: service type, make, model
        if (!existing.getServiceType().equals(equipReq.getServiceType()) ||
            !existing.getBrand().equals(brand) ||
            !existing.getModel().equals(model)) {
            return false;
        }
        
        // For mount services, boot must also match
        if ("MOUNT".equals(equipReq.getServiceType())) {
            if (existing.getBoot() == null || boot == null) {
                return existing.getBoot() == null && boot == null;
            }
            return existing.getBoot().getId().equals(boot.getId());
        }
        
        return true;
    }

    /**
     * Determines if an Equipment can be merged based on its status.
     * Rules:
     * - PICKED_UP or work order COMPLETED: never merge (always create new)
     * - IN_PROGRESS or work order RECEIVED: can merge
     * 
     * @param equipment the existing equipment item
     * @return true if item can be merged
     */
    private boolean canMergeEquipment(Equipment equipment) {
        String itemStatus = equipment.getStatus();
        String workOrderStatus = equipment.getWorkOrder().getStatus();
        
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
     * Creates a new Equipment and adds it to the work order.
     * Equipment is owned by Customer, so it must be persisted via Customer cascade.
     * 
     * @param customer the customer
     * @param workOrder the work order
     * @param equipReq the equipment item request
     * @param boot the boot (null for non-mount services)
     */
    private void createNewEquipment(Customer customer, WorkOrder workOrder, EquipmentItemRequest equipReq, Boot boot) {
        Equipment equipment;
        
        // Use newEquipment if provided, otherwise fall back to legacy fields
        if (equipReq.getNewEquipment() != null) {
            EquipmentRequest newEquip = equipReq.getNewEquipment();
            equipment = new Equipment(
                newEquip.getBrand(),
                newEquip.getModel(),
                equipReq.getServiceType()
            );
            // Set additional fields from newEquipment
            if (newEquip.getLength() != null) {
                equipment.setLength(newEquip.getLength());
            }
            equipment.setCondition(newEquip.getCondition());
            equipment.setAbilityLevel(newEquip.getAbilityLevel());
            // Set type if provided, default to SKI
            equipment.setType(newEquip.getType() != null ? newEquip.getType() : Equipment.EquipmentType.SKI);
        } else {
            // Legacy mode: use skiMake and skiModel
            equipment = new Equipment(
                equipReq.getSkiMake(),
                equipReq.getSkiModel(),
                equipReq.getServiceType()
            );
            // Set condition if provided in legacy mode
            if (equipReq.getCondition() != null) {
                equipment.setCondition(equipReq.getCondition());
            }
        }
        
        equipment.setStatus(EquipmentStatus.PENDING.name()); // All new items start as PENDING
        
        // Handle mount-specific requirements
        if ("MOUNT".equals(equipReq.getServiceType())) {
            // Populate basic equipment data (binding info, condition)
            populateBasicEquipmentData(equipment, equipReq);
            
            // Attach boot and populate profile data
            attachBootToEquipment(equipment, equipReq, boot);
            
            // Validate all required fields are present
            validateMountRequirements(equipment);
            
            // Update customer profile with any new information
            updateCustomerProfileFromEquipment(customer, equipment);
        }
        
        // IMPORTANT: Customer owns Equipment lifecycle
        // 1. Add equipment to customer (sets bidirectional relationship)
        customer.addEquipment(equipment);
        
        // 2. Attach to work order (reference only, no cascade)
        // Guard: Prevent duplicate equipment entries in work order
        if (!workOrder.getEquipment().contains(equipment)) {
            workOrder.addEquipment(equipment);
        }
        
        // 3. Save via customer cascade (this automatically persists equipment with work_order_id)
        customerRepository.save(customer);
    }

    /**
     * Finds an existing active work order for the customer that contains equipment
     * matching the provided details. This prevents creating duplicate work orders
     * when the customer already has an active order with the same equipment.
     * 
     * Active statuses: RECEIVED, IN_PROGRESS (excludes PICKED_UP and COMPLETED)
     * 
     * Match criteria:
     * - Brand matches
     * - Model matches
     * - Length matches (if provided)
     * - Boot ID matches (for MOUNT services)
     * - Service type matches
     * 
     * @param customer the customer
     * @param equipReq the equipment item request with equipment details
     * @param boot the boot (null for non-mount services)
     * @return Optional containing the matching work order, or empty if no match
     */
    private Optional<WorkOrder> findDuplicateActiveWorkOrder(
        Customer customer,
        EquipmentItemRequest equipReq,
        Boot boot
    ) {
        // Query for active work orders (RECEIVED or IN_PROGRESS only)
        List<String> activeStatuses = Arrays.asList(
            WorkOrderStatus.RECEIVED.name(),
            WorkOrderStatus.IN_PROGRESS.name()
        );
        
        List<WorkOrder> activeWorkOrders = 
            workOrderRepository.findByCustomerIdAndStatusIn(customer.getId(), activeStatuses);
        
        // Check each active work order for matching equipment
        for (WorkOrder workOrder : activeWorkOrders) {
            for (Equipment equipment : workOrder.getEquipment()) {
                // Skip picked up items (they're no longer active)
                if ("PICKED_UP".equals(equipment.getStatus())) {
                    continue;
                }
                
                // Check if equipment matches the request
                if (equipmentMatchesRequest(equipment, equipReq, boot)) {
                    return Optional.of(workOrder);
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Checks if an Equipment entity matches an EquipmentItemRequest.
     * 
     * Match criteria:
     * - Service type matches
     * - Brand matches
     * - Model matches
     * - Boot ID matches (for MOUNT services only)
     * 
     * @param equipment the equipment to check
     * @param equipReq the equipment item request
     * @param boot the boot for mount services (null for others)
     * @return true if equipment matches the request
     */
    private boolean equipmentMatchesRequest(Equipment equipment, EquipmentItemRequest equipReq, Boot boot) {
        // Match service type
        if (!equipment.getServiceType().equals(equipReq.getServiceType())) {
            return false;
        }
        
        // Get brand and model from appropriate source
        String brand = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getBrand() 
            : equipReq.getSkiMake();
        String model = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getModel() 
            : equipReq.getSkiModel();
        
        // Match brand
        if (!equipment.getBrand().equals(brand)) {
            return false;
        }
        
        // Match model
        if (!equipment.getModel().equals(model)) {
            return false;
        }
        
        // For MOUNT services, boot must also match
        if ("MOUNT".equals(equipReq.getServiceType())) {
            if (equipment.getBoot() == null || boot == null) {
                return equipment.getBoot() == null && boot == null;
            }
            return equipment.getBoot().getId().equals(boot.getId());
        }
        
        return true;
    }

    /**
     * Creates a unique key for an equipment item request to detect duplicates within the same request.
     * Key includes: serviceType, brand, model, and bootId (for mount services).
     * 
     * @param equipReq the equipment item request
     * @return unique key string
     */
    private String createEquipmentItemKey(EquipmentItemRequest equipReq) {
        StringBuilder key = new StringBuilder();
        key.append(equipReq.getServiceType()).append("|");
        
        // Get brand and model from appropriate source
        String brand = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getBrand() 
            : equipReq.getSkiMake();
        String model = equipReq.getNewEquipment() != null 
            ? equipReq.getNewEquipment().getModel() 
            : equipReq.getSkiModel();
        
        key.append(brand).append("|");
        key.append(model).append("|");
        
        // For mount services, include boot information to distinguish different boots
        if ("MOUNT".equals(equipReq.getServiceType())) {
            if (equipReq.getBootId() != null) {
                key.append("bootId:").append(equipReq.getBootId());
            } else {
                // For new boots, use boot brand/model/bsl as distinguisher
                key.append("boot:").append(equipReq.getBootBrand())
                   .append("|").append(equipReq.getBootModel())
                   .append("|").append(equipReq.getBsl());
            }
        }
        
        return key.toString();
    }

    /**
     * Attaches boot to equipment and populates profile data.
     * 
     * @param equipment the equipment item
     * @param equipReq the original request
     * @param boot the boot to attach
     */
    private void attachBootToEquipment(Equipment equipment, EquipmentItemRequest equipReq, Boot boot) {
        // Attach boot to equipment.
        // IMPORTANT: Do NOT add the Equipment to boot.getSkiItems() here.
        // Boot has cascade=ALL to Equipment, and adding to that collection can introduce
        // an additional cascade-persist path. We want persistence for new Equipment to
        // flow ONLY through WorkOrder -> Equipment cascade.
        equipment.setBoot(boot);
        
        // Populate Equipment with profile data
        if (boot.getHeightInches() != null) {
            equipment.setHeightInches(boot.getHeightInches());
        } else {
            equipment.setHeightInches(equipReq.getHeightInches());
        }
        
        if (boot.getWeight() != null) {
            equipment.setWeight(boot.getWeight());
        } else {
            equipment.setWeight(equipReq.getWeight());
        }
        
        if (boot.getAge() != null) {
            equipment.setAge(boot.getAge());
        } else {
            equipment.setAge(equipReq.getAge());
        }
        
        if (boot.getAbilityLevel() != null) {
            equipment.setAbilityLevel(boot.getAbilityLevel());
        } else {
            equipment.setAbilityLevel(equipReq.getSkiAbilityLevel());
        }
    }

    /**
     * Updates the status of a specific equipment item and recalculates the work order status.
     * Enforces business rules for valid item status transitions.
     * 
     * Valid item status transitions:
     * - PENDING → IN_PROGRESS
     * - IN_PROGRESS → DONE
     * - Any status → PICKED_UP (only via pickup workflow)
     * 
     * Process:
     * 1. Find and validate the equipment item
     * 2. Validate the status transition
     * 3. Update the item status
     * 4. Recalculate work order status (item-driven)
     * 5. Save via Customer (owns Equipment lifecycle)
     * 6. Return the work order
     * 
     * @param workOrderId the ID of the work order
     * @param equipmentId the ID of the equipment item to update
     * @param newStatus the new status for the equipment item
     * @return the updated work order
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public WorkOrder updateEquipmentStatus(Long workOrderId, Long equipmentId, String newStatus) {
        // Find the work order
        WorkOrder workOrder = workOrderRepository.findByIdWithEquipment(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // Find the equipment item within this work order
        Equipment equipment = workOrder.getEquipment().stream()
            .filter(item -> item.getId().equals(equipmentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Equipment item not found in work order: " + equipmentId));

        // Validate the status transition
        validateItemStatusTransition(equipment.getStatus(), newStatus);
        
        // Prevent manual setting of PICKED_UP (only via pickup workflow)
        if ("PICKED_UP".equals(newStatus)) {
            throw new IllegalArgumentException("PICKED_UP status can only be set via pickup workflow, not manually");
        }

        // Update the equipment item's status
        equipment.setStatus(newStatus);

        // Recalculate work order status based on all items (item-driven logic)
        updateWorkOrderStatusAndCompletedDate(workOrder);

        // Save via Customer (owns Equipment lifecycle)
        Customer customer = workOrder.getCustomer();
        customerRepository.save(customer);
        
        // Save work order status change
        workOrderRepository.save(workOrder);

        return workOrder;
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
     * Updates work order status based on equipment items and manages completedDate.
     * 
     * BUSINESS RULES:
     * - When status transitions TO COMPLETED: set completedDate = now()
     * - When status transitions FROM COMPLETED to any other: clear completedDate = null
     * - Protects against partial completion (all items must be PICKED_UP for COMPLETED)
     * 
     * This is the centralized method for status updates to ensure completedDate
     * is always synchronized with the COMPLETED status.
     * 
     * @param workOrder the work order to update
     */
    private void updateWorkOrderStatusAndCompletedDate(WorkOrder workOrder) {
        String oldStatus = workOrder.getStatus();
        workOrder.updateStatusBasedOnItems();
        String newStatus = workOrder.getStatus();
        
        // Set completedDate when transitioning TO COMPLETED
        if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(oldStatus)) {
            workOrder.setCompletedDate(LocalDateTime.now());
        }
        
        // Clear completedDate when transitioning FROM COMPLETED to any other status
        if (!"COMPLETED".equals(newStatus) && "COMPLETED".equals(oldStatus)) {
            workOrder.setCompletedDate(null);
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
        updateWorkOrderStatusAndCompletedDate(workOrder);
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
        WorkOrder workOrder = workOrderRepository.findByIdWithEquipment(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // VALIDATION: Work order must be awaiting pickup (customer notified)
        if (!"AWAITING_PICKUP".equals(workOrder.getStatus())) {
            throw new IllegalArgumentException(
                String.format("Cannot pickup work order in status: %s. Must be AWAITING_PICKUP (customer notified).", 
                    workOrder.getStatus()));
        }

        // VALIDATION: ALL items must be DONE (no partial pickups allowed)
        List<Equipment> nonDoneItems = workOrder.getEquipment().stream()
            .filter(item -> !"DONE".equals(item.getStatus()))
            .collect(Collectors.toList());
        
        if (!nonDoneItems.isEmpty()) {
            String itemStatuses = nonDoneItems.stream()
                .map(item -> String.format("%s %s (%s): %s", 
                    item.getBrand(), item.getModel(), item.getServiceType(), item.getStatus()))
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                String.format("Cannot pickup work order: %d items are not DONE: %s. All items must be DONE before pickup.",
                    nonDoneItems.size(), itemStatuses));
        }

        // ATOMIC OPERATION: Update ALL items to PICKED_UP
        workOrder.getEquipment().forEach(item -> {
            item.setStatus("PICKED_UP");
        });
        
        // Set work order status to COMPLETED and record completion timestamp
        workOrder.setStatus(WorkOrderStatus.COMPLETED.name());
        workOrder.setCompletedDate(LocalDateTime.now());
        
        // Save via Customer (owns Equipment lifecycle)
        Customer customer = workOrder.getCustomer();
        customerRepository.save(customer);
        
        // Save work order status change (transactional - either all succeed or all fail)
        workOrderRepository.save(workOrder);
        return workOrder;
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
        WorkOrder workOrder = workOrderRepository.findByIdWithEquipment(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // VALIDATION: Work order must be ready for pickup
        if (!"READY_FOR_PICKUP".equals(workOrder.getStatus())) {
            throw new IllegalArgumentException(
                String.format("Cannot notify customer for work order in status: %s. Must be READY_FOR_PICKUP.", 
                    workOrder.getStatus()));
        }

        // VALIDATION: ALL items must be DONE (double-check business rules)
        List<Equipment> nonDoneItems = workOrder.getEquipment().stream()
            .filter(item -> !"DONE".equals(item.getStatus()))
            .collect(Collectors.toList());
        
        if (!nonDoneItems.isEmpty()) {
            String itemStatuses = nonDoneItems.stream()
                .map(item -> String.format("%s %s (%s): %s", 
                    item.getBrand(), item.getModel(), item.getServiceType(), item.getStatus()))
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
        workOrderRepository.save(workOrder);
        return workOrder;
    }

    /**
     * Basic save method for persisting work orders.
     * 
     * @param order the work order to save
     * @return the saved work order
     */
    public WorkOrder save(WorkOrder order) {
        workOrderRepository.save(order);
        return order;
    }

    /**
     * Get all completed work orders, optionally filtered by equipment service type.
     * Returns work orders with status COMPLETED, sorted by completion date (most recent first).
     * 
     * @param serviceType optional service type filter (e.g., "TUNE", "WAX", "EDGE", "BASE_REPAIR")
     * @return list of completed work orders, filtered by service type if provided
     */
    public List<WorkOrder> getCompletedWorkOrders(String serviceType) {
        List<WorkOrder> completedWorkOrders = workOrderRepository.findCompletedWorkOrdersOrderByCompletedDateDesc();
        
        // If no service type filter, return all completed work orders
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return completedWorkOrders;
        }
        
        // Filter by service type: only include work orders that have at least one equipment item with the specified service type
        String filterServiceType = serviceType.trim().toUpperCase();
        return completedWorkOrders.stream()
            .filter(workOrder -> workOrder.getEquipment().stream()
                .anyMatch(equipment -> filterServiceType.equalsIgnoreCase(equipment.getServiceType())))
            .collect(Collectors.toList());
    }

    /**
     * Validates that all required fields are present for MOUNT service type.
     * 
     * @param equipReq the equipment item request to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateMountRequirements(EquipmentItemRequest equipReq) {
        if (equipReq.getBindingBrand() == null || equipReq.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (equipReq.getBindingModel() == null || equipReq.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        if (equipReq.getBootBrand() == null || equipReq.getBootBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot brand is required for MOUNT service");
        }
        if (equipReq.getBootModel() == null || equipReq.getBootModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot model is required for MOUNT service");
        }
        if (equipReq.getBsl() == null) {
            throw new IllegalArgumentException("BSL (Boot Sole Length) is required for MOUNT service");
        }
        if (equipReq.getHeightInches() == null) {
            throw new IllegalArgumentException("Height is required for MOUNT service");
        }
        if (equipReq.getWeight() == null) {
            throw new IllegalArgumentException("Weight is required for MOUNT service");
        }
        if (equipReq.getAge() == null) {
            throw new IllegalArgumentException("Age is required for MOUNT service");
        }
        if (equipReq.getSkiAbilityLevel() == null) {
            throw new IllegalArgumentException("Ski ability level is required for MOUNT service");
        }
        if (equipReq.getCondition() == null) {
            throw new IllegalArgumentException("Condition is required for MOUNT service");
        }
    }

    /**
     * Validates that all required fields are present for MOUNT service type on an Equipment.
     * 
     * @param equipment the equipment to validate
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateMountRequirements(Equipment equipment) {
        if (equipment.getBindingBrand() == null || equipment.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (equipment.getBindingModel() == null || equipment.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        if (equipment.getBoot() == null) {
            throw new IllegalArgumentException("Boot information is required for MOUNT service");
        }
        Boot boot = equipment.getBoot();
        if (boot.getBrand() == null || boot.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot brand is required for MOUNT service");
        }
        if (boot.getModel() == null || boot.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Boot model is required for MOUNT service");
        }
        if (boot.getBsl() == null) {
            throw new IllegalArgumentException("BSL (Boot Sole Length) is required for MOUNT service");
        }
        if (equipment.getHeightInches() == null) {
            throw new IllegalArgumentException("Height is required for MOUNT service");
        }
        if (equipment.getWeight() == null) {
            throw new IllegalArgumentException("Weight is required for MOUNT service");
        }
        if (equipment.getAge() == null) {
            throw new IllegalArgumentException("Age is required for MOUNT service");
        }
        if (equipment.getAbilityLevel() == null) {
            throw new IllegalArgumentException("Ski ability level is required for MOUNT service");
        }
        if (equipment.getCondition() == null) {
            throw new IllegalArgumentException("Condition is required for MOUNT service");
        }
    }

    /**
     * Auto-fills missing mount fields from customer's skier profile.
     * Only fills skier profile fields (height, weight, ability) - NOT boot information.
     * Boot attachment is handled separately in attachBootToEquipment method.
     * Only fills fields that are null in the equipment but have values in customer profile.
     * Does NOT auto-fill binding brand/model as those are ski-specific.
     * 
     * @param customer the customer with potential profile data
     * @param equipment the equipment to auto-fill
     * @param equipReq the original request (for reference only)
     */
    private void autoFillFromCustomerProfile(Customer customer, Equipment equipment, EquipmentItemRequest equipReq) {
        // Auto-fill skier profile fields from customer if missing in equipment
        if (equipment.getHeightInches() == null && customer.getHeightInches() != null) {
            equipment.setHeightInches(customer.getHeightInches());
        }
        if (equipment.getWeight() == null && customer.getWeight() != null) {
            equipment.setWeight(customer.getWeight());
        }
        if (equipment.getAbilityLevel() == null && customer.getSkiAbilityLevel() != null) {
            equipment.setAbilityLevel(customer.getSkiAbilityLevel());
        }
        
        // NOTE: Boot assignment is handled separately in attachBootToEquipment method.
        // This method only handles skier profile auto-fill to avoid conflicts.
    }

    /**
     * Validates that equipment selection is properly specified.
     * Rules:
     * - Only ONE of equipmentId or newEquipment should be provided
     * - If neither is provided, fall back to legacy fields (skiMake, skiModel) for backward compatibility
     * 
     * @param equipReq the equipment item request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEquipmentSelection(EquipmentItemRequest equipReq) {
        boolean hasEquipmentId = equipReq.getEquipmentId() != null;
        boolean hasNewEquipment = equipReq.getNewEquipment() != null;
        boolean hasLegacyFields = equipReq.getSkiMake() != null && equipReq.getSkiModel() != null;
        
        // Both equipmentId and newEquipment provided - error
        if (hasEquipmentId && hasNewEquipment) {
            throw new IllegalArgumentException("Cannot provide both equipmentId and newEquipment - only one should be specified");
        }
        
        // New API: equipmentId or newEquipment required
        if (hasEquipmentId || hasNewEquipment) {
            return; // Valid
        }
        
        // Legacy API: skiMake and skiModel required
        if (!hasLegacyFields) {
            throw new IllegalArgumentException("Either equipmentId, newEquipment, or (skiMake + skiModel) must be provided");
        }
    }

    /**
     * Validates mount request requirements.
     * 
     * @param customer the requesting customer
     * @param equipReq the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateMountRequest(Customer customer, EquipmentItemRequest equipReq) {
        // Validate binding information
        if (equipReq.getBindingBrand() == null || equipReq.getBindingBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding brand is required for MOUNT service");
        }
        if (equipReq.getBindingModel() == null || equipReq.getBindingModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Binding model is required for MOUNT service");
        }
        
        // Boot validation: either bootId OR (bootBrand + bootModel + bsl) required
        boolean hasBootId = equipReq.getBootId() != null;
        boolean hasBootInfo = equipReq.getBootBrand() != null && 
                             equipReq.getBootModel() != null && 
                             equipReq.getBsl() != null;
        
        if (!hasBootId && !hasBootInfo) {
            throw new IllegalArgumentException("Either bootId or (bootBrand + bootModel + bsl) is required for MOUNT service");
        }
        
        // If creating new boot, additional fields are required
        if (!hasBootId) {
            if (equipReq.getHeightInches() == null) {
                throw new IllegalArgumentException("heightInches is required when creating new boot for MOUNT service");
            }
            if (equipReq.getWeight() == null) {
                throw new IllegalArgumentException("weight is required when creating new boot for MOUNT service");
            }
            if (equipReq.getAge() == null) {
                throw new IllegalArgumentException("age is required when creating new boot for MOUNT service");
            }
            if (equipReq.getSkiAbilityLevel() == null) {
                throw new IllegalArgumentException("abilityLevel is required when creating new boot for MOUNT service"); 
            }
        }
    }

    /**
     * DEPRECATED: Use handleBootForMount and attachBootToEquipment instead.
     * This method is kept for backward compatibility but should not be used
     * in the new duplication prevention logic.
     * 
     * @deprecated Use handleBootForMount and attachBootToEquipment instead
     */
    @Deprecated
    private void attachBootAndPopulateProfile(Customer customer, Equipment equipment, EquipmentItemRequest equipReq) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use handleBootForMount and attachBootToEquipment instead."
        );
    }

    /**
     * Populates equipment with basic mount-specific data from request.
     * Only handles binding information and condition - not boot profile data.
     * Boot profile data is handled separately in attachBootAndPopulateProfile.
     * 
     * @param equipment the equipment to populate
     * @param equipReq the request containing the data
     */
    private void populateBasicEquipmentData(Equipment equipment, EquipmentItemRequest equipReq) {
        // Set binding information (always from request)
        equipment.setBindingBrand(equipReq.getBindingBrand());
        equipment.setBindingModel(equipReq.getBindingModel());
        
        // Set condition from the appropriate source
        if (equipReq.getNewEquipment() != null) {
            // Using newEquipment mode - condition already set during equipment creation
            // Don't overwrite it
        } else {
            // Legacy mode - get condition from top-level field
            equipment.setCondition(equipReq.getCondition());
        }
        
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
     * Saves skier profile data from equipment to customer for future auto-population.
     * Only overwrites customer data if new values are provided.
     * Boot information is automatically saved through the Boot entity relationship.
     * 
     * @param customer the customer to update
     * @param equipment the equipment containing skier data
     */
    /**
     * Updates customer profile with skier information from Equipment when creating new boots.
     * Only updates customer profile if the Equipment has more recent or complete information.
     * 
     * @param customer the customer to update
     * @param equipment the equipment containing skier data
     */
    private void updateCustomerProfileFromEquipment(Customer customer, Equipment equipment) {
        // Update customer profile with skier information (for profile consistency)
        if (equipment.getHeightInches() != null && customer.getHeightInches() == null) {
            customer.setHeightInches(equipment.getHeightInches());
        }
        if (equipment.getWeight() != null && customer.getWeight() == null) {
            customer.setWeight(equipment.getWeight());
        }
        if (equipment.getAbilityLevel() != null && customer.getSkiAbilityLevel() == null) {
            customer.setSkiAbilityLevel(equipment.getAbilityLevel());
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
     * Use updateEquipmentStatus() to change individual item statuses, which will
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
            "Use updateEquipmentStatus() to change item statuses, which will automatically " +
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
