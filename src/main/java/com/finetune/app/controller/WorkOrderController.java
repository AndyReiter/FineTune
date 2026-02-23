package com.finetune.app.controller;

import com.finetune.app.model.dto.CreateWorkOrderRequest;
import com.finetune.app.model.dto.WorkOrderResponse;
import com.finetune.app.model.dto.UpdateEquipmentStatusRequest;
import com.finetune.app.model.dto.BootResponse;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.model.SignedAgreement;
import com.finetune.app.repository.sql.WorkOrderSqlRepository;
import com.finetune.app.repository.sql.CustomerSqlRepository;
import com.finetune.app.service.WorkOrderService;
import com.finetune.app.service.SignedAgreementService;
import com.finetune.app.service.ObjectStorageService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * WorkOrderController handles all WorkOrder-related API endpoints.
 * 
 * Key Changes:
 * - Now delegates to WorkOrderService for merge logic (moved from controller)
 * - POST /workorders calls WorkOrderService.createOrMergeWorkOrder()
 * - Returns WorkOrderResponse DTOs for clean JSON (no circular references)
 * 
 * Merge Behavior:
 * - If customer exists with an active work order (RECEIVED or IN_PROGRESS), new equipment items are MERGED
 * - If no active work order exists, a NEW work order is created
 * - Completed or picked-up work orders are never reused; new orders are created instead
 * - Individual equipment item status is tracked
 * - Overall work order status is DONE only when ALL items are DONE
 * 
 * All GET endpoints return WorkOrderResponse DTOs to maintain clean JSON structure.
 */
@RestController
@RequestMapping("/workorders")
@CrossOrigin(origins = "*")
public class WorkOrderController {

    @Autowired
    private WorkOrderSqlRepository workOrderRepository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CustomerSqlRepository customerRepository;

    @Autowired
    private SignedAgreementService signedAgreementService;

    @Autowired
    private ObjectStorageService objectStorageService;

    /**
     * Get all work orders, optionally filtered by status.
     * Returns WorkOrderResponse DTOs to avoid circular references.
     * 
     * @param status optional status filter (e.g., "RECEIVED", "IN_PROGRESS", "READY_FOR_PICKUP", "COMPLETED")
     *               Note: "COMPLETED" will return both "COMPLETED" and "PICKED_UP" work orders
     * @return list of work orders, filtered by status if provided
     */
    @GetMapping
    public List<WorkOrderResponse> getAllWorkOrders(@RequestParam(value = "status", required = false) String status) {
        List<WorkOrder> workOrders;
        
        if (status != null && !status.trim().isEmpty()) {
            String statusFilter = status.trim();
            
            if ("COMPLETED".equals(statusFilter)) {
                // For "COMPLETED" tab, show both COMPLETED and PICKED_UP work orders
                workOrders = workOrderRepository.findByStatusInOrderByCreatedAtAsc(List.of("COMPLETED", "PICKED_UP"));
            } else {
                // Filter by specific status
                workOrders = workOrderRepository.findByStatusOrderByCreatedAtAsc(statusFilter);
            }
        } else {
            // Return all work orders
            workOrders = workOrderRepository.findAllOrderByCreatedAtAsc();
        }
        
        return workOrders.stream()
            .map(WorkOrderResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Create or merge a work order.
     * 
     * Workflow (delegated to WorkOrderService):
     * 1. Validates the incoming request (customer info + equipment items)
     * 2. Finds existing Customer by email/phone OR creates new one
     * 3. Searches for an open work order for that customer
     * 4. If found: MERGE new equipment items into the existing work order
     *    - No notification sent (customer already notified)
     * 5. If not found: CREATE a new work order
     *    - Notification would be sent to customer
     * 6. Updates work order status based on all equipment items
     *    - Status = DONE only when ALL items are DONE
     * 7. Persists changes (cascade handles Customer, WorkOrder, Equipment)
     * 8. Returns the work order (new or merged) as WorkOrderResponse DTO
     * 
     * @param request CreateWorkOrderRequest with customer and equipment item details
     * @return WorkOrderResponse with all equipment items (new + merged)
     */
    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request) {

        // Delegate to service which handles:
        // - Customer lookup/creation
        // - Open work order detection
        // - Merging items into existing orders
        // - Creating new orders when needed
        WorkOrder workOrder = workOrderService.createOrMergeWorkOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Get a specific work order by ID with all its equipment items.
     * 
     * @param id WorkOrder ID
     * @return WorkOrderResponse with customer info and all equipment items, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> getWorkOrderById(@PathVariable Long id) {
        return workOrderRepository.findByIdWithEquipment(id)
            .map(workOrder -> ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get customer boots by email and phone for boot selection workflow.
     * Used in the dashboard two-modal system for MOUNT service work orders.
     * 
     * @param email Customer email (required)
     * @param phone Customer phone (required)
     * @return List of BootResponse objects, or 404 if customer not found
     */
    @GetMapping("/customer/boots")
    public ResponseEntity<List<BootResponse>> getCustomerBoots(
            @RequestParam String email, 
            @RequestParam String phone) {
        return customerRepository.findByEmailAndPhone(email, phone)
            .map(customer -> {
                List<BootResponse> boots = customer.getBoots()
                    .stream()
                    .map(BootResponse::fromEntity)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(boots);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mark a work order as picked up (closed).
     * Once picked up, no new items can be merged into this order.
     * New items for this customer will create a new work order.
     * 
     * @param id WorkOrder ID
     * @return Updated WorkOrderResponse, or 404 if not found
     */
    @PostMapping("/{id}/pickup")
    public ResponseEntity<WorkOrderResponse> pickupWorkOrder(@PathVariable Long id) {
        WorkOrder workOrder = workOrderService.pickupWorkOrder(id);
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Notify customer that work order is ready for pickup.
     * Transitions work order from READY_FOR_PICKUP to AWAITING_PICKUP.
     * 
     * @param id WorkOrder ID
     * @return Updated WorkOrderResponse, or 404 if not found
     */
    @PostMapping("/{id}/notify")
    public ResponseEntity<WorkOrderResponse> notifyCustomer(@PathVariable Long id) {
        WorkOrder workOrder = workOrderService.notifyCustomer(id);
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Update an equipment item's status within a work order.
     * ENFORCES ITEM-DRIVEN STATUS TRANSITIONS.
     * 
     * Allowed item statuses: PENDING, IN_PROGRESS, DONE
     * PICKED_UP status can ONLY be set via pickup workflow, not manually.
     * 
     * Valid transitions:
     * - PENDING → IN_PROGRESS
     * - IN_PROGRESS → DONE (or back to PENDING)
     * - DONE → IN_PROGRESS (allows rework)
     * 
     * Work order status is automatically recalculated based on ALL item statuses:
     * - RECEIVED: all items are PENDING
     * - IN_PROGRESS: at least one item is IN_PROGRESS  
     * - READY_FOR_PICKUP: all items are DONE
     * - COMPLETED: all items are PICKED_UP
     * 
     * @param orderId WorkOrder ID
     * @param equipmentId Equipment Item ID
     * @param request UpdateEquipmentStatusRequest with new status
     * @return Updated WorkOrderResponse with recalculated work order status
     * @throws IllegalArgumentException if status transition is invalid
     */
    @PatchMapping("/{orderId}/equipment/{equipmentId}/status")
    public ResponseEntity<WorkOrderResponse> updateEquipmentStatus(
            @PathVariable Long orderId,
            @PathVariable Long equipmentId,
            @Valid @RequestBody UpdateEquipmentStatusRequest request) {
        
        WorkOrder workOrder = workOrderService.updateEquipmentStatus(orderId, equipmentId, request.getStatus());
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Update the promisedBy (due date) for a work order.
     * 
     * @param id WorkOrder ID
     * @param request Map containing "promisedBy" field (LocalDate string or null)
     * @return Updated WorkOrderResponse
     */
    @PatchMapping("/{id}/promised-by")
    public ResponseEntity<WorkOrderResponse> updatePromisedBy(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> request) {
        
        WorkOrder workOrder = workOrderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Work order not found"));
        
        // Handle promisedBy update (can be null to clear the date)
        Object promisedByValue = request.get("promisedBy");
        if (promisedByValue == null) {
            workOrder.setPromisedBy(null);
        } else {
            workOrder.setPromisedBy(java.time.LocalDate.parse(promisedByValue.toString()));
        }
        
        workOrderRepository.save(workOrder);
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Update work order fields (general PATCH endpoint).
     * Accepts partial updates for notes and promisedBy fields.
     * 
     * @param id WorkOrder ID
     * @param request Map containing fields to update: "notes" (String), "promisedBy" (LocalDate string or null)
     * @return Updated WorkOrderResponse
     */
    @PatchMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> updateWorkOrder(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> request) {
        
        WorkOrder workOrder = workOrderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Work order not found"));
        
        // Handle notes update
        if (request.containsKey("notes")) {
            Object notesValue = request.get("notes");
            workOrder.setNotes(notesValue != null ? notesValue.toString() : null);
        }
        
        // Handle promisedBy update (can be null to clear the date)
        if (request.containsKey("promisedBy")) {
            Object promisedByValue = request.get("promisedBy");
            if (promisedByValue == null) {
                workOrder.setPromisedBy(null);
            } else {
                workOrder.setPromisedBy(java.time.LocalDate.parse(promisedByValue.toString()));
            }
        }
        
        workOrderRepository.save(workOrder);
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Confirm intake review and activate a customer-submitted work order.
     * 
     * This transitions a work order from CUSTOMER_SUBMITTED to RECEIVED status,
     * allowing it to enter the normal workflow. This endpoint is used after staff
     * completes the intake review process:
     * - Verify equipment
     * - Edit notes
     * - Add/remove services
     * - Set promisedBy date (REQUIRED)
     * 
     * @param id WorkOrder ID
     * @return Updated WorkOrderResponse with RECEIVED status
     * @throws IllegalArgumentException if work order is not in CUSTOMER_SUBMITTED status
     * @throws IllegalStateException if promisedBy date is not set
     */
    @PostMapping("/{id}/confirm-intake")
    public ResponseEntity<WorkOrderResponse> confirmIntake(@PathVariable Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + id));
        
        // Validate work order is in CUSTOMER_SUBMITTED status
        if (!"CUSTOMER_SUBMITTED".equals(workOrder.getStatus())) {
            throw new IllegalArgumentException(
                "Work order must be in CUSTOMER_SUBMITTED status. Current status: " + workOrder.getStatus()
            );
        }
        
        // Validate promisedBy date is set
        if (workOrder.getPromisedBy() == null) {
            throw new IllegalStateException(
                "Cannot activate work order without a promised date. Please set a due date first."
            );
        }
        
        // Update equipment service history
        java.time.LocalDate today = java.time.LocalDate.now();
        if (workOrder.getEquipment() != null) {
            workOrder.getEquipment().forEach(equipment -> {
                equipment.setLastServicedDate(today);
                equipment.setLastServiceType(equipment.getServiceType());
            });
        }
        
        // Transition to RECEIVED status (entering normal workflow)
        workOrder.setStatus("RECEIVED");
        workOrderRepository.save(workOrder);
        
        return ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder));
    }

    /**
     * Delete a work order by ID.
     * Note: WorkOrder is cascaded to delete from Customer.
     * 
     * @param id WorkOrder ID
     * @return 200 OK if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        if (workOrderRepository.existsById(id)) {
            workOrderRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get all completed work orders, optionally filtered by service type.
     * Returns work orders with status COMPLETED, sorted by completion date (most recent first).
     * 
     * Endpoint: GET /workorders/completed
     * Query Params:
     * - serviceType (optional): Filter by equipment service type (e.g., "TUNE", "WAX", "EDGE", "BASE_REPAIR")
     * 
     * @param serviceType optional service type filter
     * @return list of completed work orders as WorkOrderResponse DTOs
     */
    @GetMapping("/completed")
    public List<WorkOrderResponse> getCompletedWorkOrders(
            @RequestParam(value = "serviceType", required = false) String serviceType) {
        
        List<WorkOrder> completedWorkOrders = workOrderService.getCompletedWorkOrders(serviceType);
        
        return completedWorkOrders.stream()
            .map(WorkOrderResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get signed URL for viewing work order agreement PDF.
     * Returns a Cloudflare R2 signed URL valid for 15 minutes.
     * Frontend should open this URL in a new tab.
     * 
     * Endpoint: GET /workorders/{id}/agreement-url
     * 
     * @param id Work Order ID
     * @return JSON with signed URL, or 404 if work order or agreement not found
     */
    @GetMapping("/{id}/agreement-url")
    public ResponseEntity<Map<String, String>> getAgreementUrl(@PathVariable Long id) {
        // Find the signed agreement for this work order
        SignedAgreement agreement = signedAgreementService.findByWorkOrderId(id);
        
        if (agreement == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Generate signed URL from R2 (15-minute default expiration)
        String signedUrl = objectStorageService.generateSignedUrl(agreement.getStorageKey());
        
        return ResponseEntity.ok(Map.of("url", signedUrl));
    }
}
