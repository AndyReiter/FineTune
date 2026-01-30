/**
 * The `WorkOrderService` class is a Spring service component that interacts with a
 * `WorkOrderRepository` to save work orders.
 */
package com.finetune.app.service;

import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderService {

    private final WorkOrderRepository repository;

    public WorkOrderService(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public WorkOrder save(WorkOrder order) {
        return repository.save(order);
    }
}
