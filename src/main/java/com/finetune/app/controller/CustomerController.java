package com.finetune.app.controller;

import com.finetune.app.model.dto.CustomerResponse;
import com.finetune.app.model.dto.CustomerResponseDTO;
import com.finetune.app.model.dto.CustomerRequest;
import com.finetune.app.model.dto.WorkOrderResponse;
import com.finetune.app.model.dto.BootResponse;
import com.finetune.app.model.dto.EquipmentRequest;
import com.finetune.app.model.dto.EquipmentResponse;
import com.finetune.app.model.Customer;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.model.Equipment;
import com.finetune.app.repository.sql.CustomerSqlRepository;
import com.finetune.app.repository.sql.EquipmentSqlRepository;
import com.finetune.app.repository.sql.BootSqlRepository;
import com.finetune.app.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CustomerController handles all Customer-related API endpoints.
 * 
 * Design:
 * - GET endpoints return CustomerResponse DTOs to prevent JSON recursion
 * - Customers are primarily managed through the WorkOrder creation flow
 * - Each customer maintains a list of WorkOrders (bidirectional relationship)
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerSqlRepository customerRepository;
    private final CustomerService customerService;
    private final EquipmentSqlRepository equipmentRepository;
    private final BootSqlRepository bootRepository;

    public CustomerController(CustomerSqlRepository customerRepository, CustomerService customerService, EquipmentSqlRepository equipmentRepository, BootSqlRepository bootRepository) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.equipmentRepository = equipmentRepository;
        this.bootRepository = bootRepository;
    }

    /**
     * Get all customers.
     * Returns CustomerResponseDTO with aggregate counts (work orders, equipment, boots).
     * 
     * @return List of all customers with counts
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get a specific customer by ID.
     * Returns CustomerResponseDTO with aggregate counts.
     * To get detailed work order info (with ski items), use GET /workorders/{id}
     * 
     * @param id Customer ID
     * @return CustomerResponseDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
            .map(customer -> ResponseEntity.ok(CustomerResponseDTO.fromEntity(customer)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search for customers by email, phone, or name.
     * Accepts one of three query parameters:
     * - email: partial match (case-insensitive)
     * - phone: exact match
     * - name: partial match against first or last name (case-insensitive)
     * 
     * @param email Optional email search query
     * @param phone Optional phone search query
     * @param name Optional name search query
     * @return List of matching CustomerResponse objects
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String name
    ) {
        List<Customer> results;
        
        if (email != null && !email.trim().isEmpty()) {
            results = customerRepository.findByEmailContainingIgnoreCase(email.trim());
        } else if (phone != null && !phone.trim().isEmpty()) {
            results = customerRepository.findByPhone(phone.trim());
        } else if (name != null && !name.trim().isEmpty()) {
            results = customerRepository.findByNameContaining(name.trim());
        } else {
            return ResponseEntity.badRequest().build();
        }
        
        List<CustomerResponse> responses = results.stream()
            .map(CustomerResponse::fromEntity)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Create a new customer.
     * 
     * @param request Customer details
     * @return 201 CREATED with CustomerResponse
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Valid CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CustomerResponse.fromEntity(savedCustomer));
    }

    /**
     * Lookup customer by email and phone for boot selection workflow.
     * Returns CustomerResponse or 404 if not found.
     * 
     * @param email Customer email
     * @param phone Customer phone number
     * @return CustomerResponse or 404 if not found
     */
    @GetMapping("/lookup")
    public ResponseEntity<CustomerResponse> lookupCustomer(
        @RequestParam String email,
        @RequestParam String phone
    ) {
        return customerRepository.findByEmailAndPhone(email, phone)
            .map(customer -> ResponseEntity.ok(CustomerResponse.fromEntity(customer)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all work orders for a specific customer.
     * Returns a list of WorkOrderResponse DTOs with full ski item details.
     * 
     * @param id Customer ID
     * @return List of WorkOrderResponse objects, or 404 if customer not found
     */
    @GetMapping("/{id}/workorders")
    public ResponseEntity<List<WorkOrderResponse>> getCustomerWorkOrders(@PathVariable Long id) {
        return customerRepository.findById(id)
            .map(customer -> {
                List<WorkOrderResponse> workOrders = customer.getWorkOrders()
                    .stream()
                    .map(WorkOrderResponse::fromEntity)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(workOrders);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all boots for a specific customer.
     * Returns a list of BootResponse DTOs.
     * 
     * @param id Customer ID
     * @return List of BootResponse objects, or 404 if customer not found
     */
    @GetMapping("/{id}/boots")
    public ResponseEntity<List<BootResponse>> getCustomerBoots(@PathVariable Long id) {
        return customerRepository.findById(id)
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
     * Get all equipment for a specific customer.
     * Returns a list of EquipmentResponse DTOs.
     * 
     * @param customerId Customer ID
     * @return List of EquipmentResponse objects, or 404 if customer not found
     */
    @GetMapping("/{customerId}/equipment")
    public ResponseEntity<List<EquipmentResponse>> getCustomerEquipment(@PathVariable Long customerId) {
        // Use EquipmentSqlRepository directly to ensure equipment is loaded from DB
        return customerRepository.findById(customerId)
            .map(customer -> {
                List<com.finetune.app.model.Equipment> equipmentEntities = equipmentRepository.findByCustomerId(customerId);
                // Populate boot details for mount service items
                if (equipmentEntities != null) {
                    for (com.finetune.app.model.Equipment eq : equipmentEntities) {
                        Long bid = eq.getBootId();
                        if (bid != null) {
                            bootRepository.findById(bid).ifPresent(eq::setBoot);
                        }
                    }
                }
                List<EquipmentResponse> equipment = equipmentEntities.stream()
                    .map(EquipmentResponse::fromEntity)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(equipment);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create equipment and attach it to a customer profile.
     * Equipment is persisted independently of work orders.
     * 
     * @param customerId Customer ID
     * @param request Equipment details
     * @return 201 CREATED with EquipmentResponse
     */
    @PostMapping("/{customerId}/equipment")
    public ResponseEntity<EquipmentResponse> createEquipment(
        @PathVariable Long customerId,
        @RequestBody @Valid EquipmentRequest request
    ) {
        Equipment savedEquipment = customerService.createEquipment(customerId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(EquipmentResponse.fromEntity(savedEquipment));
    }
}