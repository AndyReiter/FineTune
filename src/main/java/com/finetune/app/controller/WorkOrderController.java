package com.finetune.app.controller;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public WorkOrder createWorkOrder(@RequestBody WorkOrder workOrder) {
        if (workOrder.getCreatedAt() == null) {
            workOrder.setCreatedAt(LocalDateTime.now());
        }
        if (workOrder.getStatus() == null) {
            workOrder.setStatus("RECEIVED");
        }
        return workOrderRepository.save(workOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        return workOrderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrder> updateWorkOrder(@PathVariable Long id, @RequestBody WorkOrder workOrderDetails) {
        return workOrderRepository.findById(id)
                .map(workOrder -> {
                    workOrder.setCustomerName(workOrderDetails.getCustomerName());
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