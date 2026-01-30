// This Java code snippet defines a repository interface `WorkOrderRepository` that extends
// `JpaRepository` interface provided by Spring Data JPA.
package com.finetune.app.repository;

import com.finetune.app.model.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
}