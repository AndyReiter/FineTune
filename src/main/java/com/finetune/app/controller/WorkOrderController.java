/**
 * The WorkOrderController class in a Java Spring application handles CRUD operations for work orders
 * and ski items.
 */
package com.finetune.app.controller;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finetune.app.model.dto.CreateWorkOrderRequest;
import com.finetune.app.model.dto.SkiItemRequest;
import com.finetune.app.model.entity.SkiItem;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workorders")
@CrossOrigin(origins = "*")
public class WorkOrderController {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @GetMapping
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    @PostMapping
    public WorkOrder createWorkOrder(
        @Valid @RequestBody CreateWorkOrderRequest request) {

        WorkOrder workOrder = new WorkOrder();
        workOrder.setCustomerFirstName(request.getCustomerFirstName());
        workOrder.setCustomerLastName(request.getCustomerLastName());
        workOrder.setPhone(request.getPhone());
        workOrder.setEmail(request.getEmail());
        workOrder.setStatus("RECEIVED");
        workOrder.setCreatedAt(LocalDateTime.now());

        for (SkiItemRequest skiReq : request.getSkis()) {
            SkiItem skiItem = new SkiItem();
            skiItem.setSkiMake(skiReq.getSkiMake());
            skiItem.setSkiModel(skiReq.getSkiModel());
            skiItem.setServiceType(skiReq.getServiceType());
            workOrder.addSkiItem(skiItem);
        }

        return workOrderRepository.save(workOrder);
    }
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        return workOrderRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a work order with the given id and details.
     * @param id The id of the work order to update.
     * @param workOrderDetails The details of the work order to update.
     * @return A ResponseEntity containing the updated work order if found, otherwise a 404 response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkOrder> updateWorkOrder(
        @PathVariable Long id,
        @RequestBody WorkOrder workOrderDetails) {

        return workOrderRepository.findById(id)
            .map(workOrder -> {
                workOrder.setCustomerFirstName(workOrderDetails.getCustomerFirstName());
                workOrder.setCustomerLastName(workOrderDetails.getCustomerLastName());
                workOrder.setPhone(workOrderDetails.getPhone());
                workOrder.setEmail(workOrderDetails.getEmail());
                workOrder.setStatus(workOrderDetails.getStatus());
                return ResponseEntity.ok(workOrderRepository.save(workOrder));
            })
            .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        if (workOrderRepository.existsById(id)) {
            workOrderRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}