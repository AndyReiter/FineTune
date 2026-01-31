package com.finetune.app.controller;

import com.finetune.app.model.dto.CustomerResponse;
import com.finetune.app.model.dto.WorkOrderResponse;
import com.finetune.app.model.entity.Customer;
import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Get all customers.
     * Returns CustomerResponse DTOs with work order summaries (no detailed ski items).
     * 
     * @return List of all customers
     */
    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(CustomerResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific customer by ID.
     * Returns CustomerResponse with work order summaries.
     * To get detailed work order info (with ski items), use GET /workorders/{id}
     * 
     * @param id Customer ID
     * @return CustomerResponse or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
            .map(customer -> ResponseEntity.ok(CustomerResponse.fromEntity(customer)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search for a customer by email address.
     * Returns CustomerResponse or 404 if not found.
     * 
     * @param email Customer email
     * @return CustomerResponse or 404 if not found
     */
    @GetMapping("/search")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(
        @RequestParam String email
    ) {
        return customerRepository.findByEmail(email)
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
}