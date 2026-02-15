package com.finetune.app.repository;

import com.finetune.app.model.entity.WorkOrderNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for WorkOrderNote entities.
 * Provides database access for work order notes.
 */
@Repository
public interface WorkOrderNoteRepository extends JpaRepository<WorkOrderNote, Long> {

    /**
     * Find all notes for a specific work order, ordered by creation date (newest first).
     * 
     * @param workOrderId the work order ID
     * @return list of notes ordered by createdAt descending
     */
    List<WorkOrderNote> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);
}
