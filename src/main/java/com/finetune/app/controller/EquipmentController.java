package com.finetune.app.controller;

import com.finetune.app.model.dto.EquipmentResponse;
import com.finetune.app.model.entity.Equipment;
import com.finetune.app.repository.EquipmentRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * EquipmentController handles all Equipment-related API endpoints.
 * Provides direct access to equipment data for debugging and frontend integration.
 */
@RestController
@RequestMapping("/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    /**
     * Get all equipment items.
     * Useful for debugging and admin purposes.
     * 
     * @return List of all equipment items
     */
    @GetMapping
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll().stream()
            .map(EquipmentResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get equipment by ID.
     * 
     * @param id Equipment ID
     * @return Equipment details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return equipmentRepository.findById(id)
            .map(equipment -> ResponseEntity.ok(EquipmentResponse.fromEntity(equipment)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get equipment by customer ID.
     * 
     * @param customerId Customer ID
     * @return List of equipment belonging to the customer
     */
    @GetMapping("/customer/{customerId}")
    public List<EquipmentResponse> getEquipmentByCustomerId(@PathVariable Long customerId) {
        return equipmentRepository.findByCustomerId(customerId).stream()
            .map(EquipmentResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get equipment by work order ID.
     * 
     * @param workOrderId Work Order ID
     * @return List of equipment linked to the work order
     */
    @GetMapping("/workorder/{workOrderId}")  
    public List<EquipmentResponse> getEquipmentByWorkOrderId(@PathVariable Long workOrderId) {
        return equipmentRepository.findByWorkOrderId(workOrderId).stream()
            .map(EquipmentResponse::fromEntity)
            .collect(Collectors.toList());
    }
}