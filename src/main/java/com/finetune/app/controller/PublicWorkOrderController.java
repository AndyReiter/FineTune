package com.finetune.app.controller;

import com.finetune.app.model.dto.CustomerEquipmentLookupResponse;
import com.finetune.app.model.dto.BootResponse;
import com.finetune.app.model.dto.EquipmentResponse;
import com.finetune.app.model.dto.CreateWorkOrderRequest;
import com.finetune.app.model.dto.WorkOrderResponse;
import com.finetune.app.model.dto.PublicWorkOrderCreationResponse;
import com.finetune.app.model.dto.SignAgreementRequest;
import com.finetune.app.model.dto.SignAgreementResponse;
import com.finetune.app.model.Shop;
import com.finetune.app.model.Customer;
import com.finetune.app.model.Equipment;
import com.finetune.app.model.SignedAgreement;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.repository.sql.CustomerSqlRepository;
import com.finetune.app.repository.sql.EquipmentSqlRepository;
import com.finetune.app.repository.sql.WorkOrderSqlRepository;
import com.finetune.app.service.*;
import com.finetune.app.util.PhoneNumberUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PublicWorkOrderController handles public-facing API endpoints for customer work orders.
 * These endpoints do not require authentication and are used by customers to:
 * - Look up their equipment and boots
 * - Create work orders with equipment selection
 */
@RestController
@RequestMapping("/api/public/workorders")
@CrossOrigin(origins = "*")
public class PublicWorkOrderController {

    private final CustomerSqlRepository customerRepository;
    private final CustomerService customerService;
    private final EquipmentSqlRepository equipmentRepository;
    private final WorkOrderService workOrderService;
    private final WorkOrderSqlRepository workOrderRepository;
    private final ObjectStorageService objectStorageService;
    private final SignedAgreementService signedAgreementService;
    private final ShopService shopService;

    public PublicWorkOrderController(
            CustomerSqlRepository customerRepository,
            CustomerService customerService,
            EquipmentSqlRepository equipmentRepository,
            WorkOrderService workOrderService,
            WorkOrderSqlRepository workOrderRepository,
            ObjectStorageService objectStorageService,
            SignedAgreementService signedAgreementService,
            ShopService shopService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.equipmentRepository = equipmentRepository;
        this.workOrderService = workOrderService;
        this.workOrderRepository = workOrderRepository;
        this.objectStorageService = objectStorageService;
        this.signedAgreementService = signedAgreementService;
        this.shopService = shopService;
    }

    /**
     * Look up customer equipment and boots by name, email, and phone.
     * Uses intelligent matching logic to find or create customer.
     * Returns customer's existing equipment and boots for selection.
     * 
     * @param name Customer's full name
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @return CustomerEquipmentLookupResponse with equipment and boots, or empty lists if new customer
     */
    @GetMapping("/lookup-equipment")
    public ResponseEntity<CustomerEquipmentLookupResponse> lookupCustomerEquipment(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone) {
        
        // Use the public customer matching logic
        Customer customer = customerService.findOrCreatePublicCustomer(name, email, phone);
        
        // Return customer's equipment and boots
        CustomerEquipmentLookupResponse response = CustomerEquipmentLookupResponse.fromEntity(customer);
        return ResponseEntity.ok(response);
    }

    /**
     * Get boots associated with a specific equipment/ski.
     * Used when customer selects a ski for mount service and needs to select boots.
     * 
     * @param equipmentId The ID of the equipment/ski
     * @return List of boots associated with this equipment, or 404 if equipment not found
     */
    @GetMapping("/equipment/{equipmentId}/boots")
    public ResponseEntity<List<BootResponse>> getBootsForEquipment(@PathVariable Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .map(equipment -> {
                List<BootResponse> boots = equipment.getBoot() != null 
                    ? List.of(BootResponse.fromEntity(equipment.getBoot()))
                    : List.of();
                return ResponseEntity.ok(boots);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all equipment for a customer by email and phone.
     * Alternative endpoint for looking up equipment without name.
     * 
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @return List of equipment, or empty list if customer not found
     */
    @GetMapping("/customer-equipment")
    public ResponseEntity<List<EquipmentResponse>> getCustomerEquipmentByContact(
            @RequestParam String email,
            @RequestParam String phone) {
        
        // Normalize phone number
        String normalizedPhone = PhoneNumberUtils.normalize(phone);
        
        return customerRepository.findByEmailOrPhone(email, normalizedPhone)
            .stream()
            .findFirst()
            .map(customer -> {
                List<EquipmentResponse> equipment = customer.getEquipment()
                    .stream()
                    .map(EquipmentResponse::fromEntity)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(equipment);
            })
            .orElse(ResponseEntity.ok(List.of())); // Return empty list if customer not found
    }

    /**
     * Get all boots for a customer by email and phone.
     * Alternative endpoint for looking up boots without name.
     * 
     * @param email Customer's email address
     * @param phone Customer's phone number
     * @return List of boots, or empty list if customer not found
     */
    @GetMapping("/customer-boots")
    public ResponseEntity<List<BootResponse>> getCustomerBootsByContact(
            @RequestParam String email,
            @RequestParam String phone) {
        
        // Normalize phone number
        String normalizedPhone = PhoneNumberUtils.normalize(phone);
        
        return customerRepository.findByEmailOrPhone(email, normalizedPhone)
            .stream()
            .findFirst()
            .map(customer -> {
                List<BootResponse> boots = customer.getBoots()
                    .stream()
                    .map(BootResponse::fromEntity)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(boots);
            })
            .orElse(ResponseEntity.ok(List.of())); // Return empty list if customer not found
    }

    /**
     * Create a new work order from public customer.
     * This endpoint enforces daily limits for customer-created work orders.
     * 
     * Response format includes work order ID, status, and equipment with associated boots.
     * 
     * @param request CreateWorkOrderRequest with customer and equipment details
     * @return PublicWorkOrderCreationResponse with work order details, or error if daily limit exceeded
     */
    @PostMapping
    public ResponseEntity<PublicWorkOrderCreationResponse> createPublicWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request) {
        
        // Create work order with customerCreated = true (enforces daily limit)
        WorkOrder workOrder = workOrderService.createOrMergeWorkOrder(request, true);
        
        // Build response with equipment options and associated boots
        PublicWorkOrderCreationResponse response = PublicWorkOrderCreationResponse.fromWorkOrder(workOrder);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Sign an agreement for a work order.
     * 
     * This is a public endpoint that requires customer verification.
     * Customer must provide email and phone matching the work order's customer.
     * 
     * Process:
     * 1. Validate work order exists
     * 2. Verify customer ownership (email + phone match)
     * 3. Check agreement not already signed
     * 4. Capture IP address and User-Agent
     * 5. Call SignedAgreementService workflow
     * 6. Return signed URL for PDF access
     * 
     * @param id Work order ID
     * @param request SignAgreementRequest with signature name, email, and phone
     * @param httpRequest HTTP request for extracting IP and User-Agent
     * @return SignAgreementResponse with PDF URL and agreement details
     */
    @PostMapping("/{id}/sign-agreement")
    public ResponseEntity<SignAgreementResponse> signAgreement(
            @PathVariable Long id,
            @Valid @RequestBody SignAgreementRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // 1. Validate work order exists
            WorkOrder workOrder = workOrderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Work order not found with id: " + id));
            
            // 2. Verify customer ownership using email and phone matching
            Customer workOrderCustomer = workOrder.getCustomer();
            String normalizedRequestPhone = PhoneNumberUtils.normalize(request.getPhone());
            String normalizedCustomerPhone = PhoneNumberUtils.normalize(workOrderCustomer.getPhone());
            
            boolean emailMatches = request.getEmail().equalsIgnoreCase(workOrderCustomer.getEmail());
            boolean phoneMatches = normalizedRequestPhone.equals(normalizedCustomerPhone);
            
            if (!emailMatches || !phoneMatches) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(SignAgreementResponse.error(
                                "Customer verification failed. Email and phone must match the work order customer."));
            }
            
            // 3. Verify agreement not already signed
            if (signedAgreementService.workOrderHasSignedAgreement(workOrder)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(SignAgreementResponse.error(
                                "Agreement already signed for this work order."));
            }
            
            // 4. Get shop (assuming single-shop system for now)
            List<Shop> shops = shopService.getAllShops();
            if (shops.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(SignAgreementResponse.error("No shop configured in the system"));
            }
            Shop shop = shops.get(0);
            
            // 5. Capture IP address and User-Agent
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 6. Complete workflow: Load template, generate PDF, upload to R2, create agreement
                System.out.println("[PublicWorkOrderController] Calling createSignedAgreementWorkflow for workOrderId=" + workOrder.getId());
                SignedAgreement signedAgreement = signedAgreementService.createSignedAgreementWorkflow(
                    workOrder,
                    shop,
                    request.getSignatureName(),
                    ipAddress,
                    userAgent,
                    request.getSignatureImageBase64()
                );
                System.out.println("[PublicWorkOrderController] createSignedAgreementWorkflow completed for workOrderId=" + workOrder.getId());
            
            // 7. Generate signed URL for PDF access (valid for 7 days)
            String pdfUrl = objectStorageService.generateSignedUrl(
                    signedAgreement.getPdfStorageKey(), 
                    Duration.ofDays(7)
            );
            
            // 8. Return success response
            return ResponseEntity.ok(SignAgreementResponse.success(
                    signedAgreement.getId(),
                    pdfUrl,
                    signedAgreement.getSignedAt(),
                    workOrder.getId()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SignAgreementResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SignAgreementResponse.error("Failed to sign agreement: " + e.getMessage()));
        }
    }

    /**
     * Extract client IP address from HTTP request.
     * Checks various headers for proxy/load balancer scenarios.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
