package com.finetune.app.service;

import com.finetune.app.model.dto.CreateWorkOrderNoteRequest;
import com.finetune.app.model.dto.WorkOrderNoteResponseDTO;
import com.finetune.app.model.WorkOrder;
import com.finetune.app.model.WorkOrderNote;
import com.finetune.app.repository.sql.WorkOrderNoteSqlRepository;
import com.finetune.app.repository.sql.WorkOrderSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing work order notes.
 * Simple append-only notes for staff use.
 */
@Service
public class WorkOrderNoteService {

    private final WorkOrderNoteSqlRepository workOrderNoteSqlRepository;
    private final WorkOrderSqlRepository workOrderSqlRepository;

    public WorkOrderNoteService(
            WorkOrderNoteSqlRepository workOrderNoteSqlRepository,
            WorkOrderSqlRepository workOrderSqlRepository) {
        this.workOrderNoteSqlRepository = workOrderNoteSqlRepository;
        this.workOrderSqlRepository = workOrderSqlRepository;
    }

    /**
     * Get all notes for a work order, ordered by creation date (newest first).
     * 
     * @param workOrderId the work order ID
     * @return list of note DTOs, newest first
     */
    public List<WorkOrderNoteResponseDTO> getNotes(Long workOrderId) {
        List<WorkOrderNote> notes = workOrderNoteSqlRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId);
        return notes.stream()
                .map(WorkOrderNoteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Add a new note to a work order.
     * 
     * @param workOrderId the work order ID
     * @param request the note creation request
     * @return the created note DTO
     * @throws IllegalArgumentException if work order not found
     */
    @Transactional
    public WorkOrderNoteResponseDTO addNote(Long workOrderId, CreateWorkOrderNoteRequest request) {
        // Validate work order exists
        WorkOrder workOrder = workOrderSqlRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // Insert note using SqlRepository
        WorkOrderNote note = workOrderNoteSqlRepository.insert(
                workOrderId,
                request.getNoteText(),
                request.getCreatedBy()
        );
        return WorkOrderNoteResponseDTO.fromEntity(note);
    }
}
