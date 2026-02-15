package com.finetune.app.service;

import com.finetune.app.model.dto.CreateWorkOrderNoteRequest;
import com.finetune.app.model.dto.WorkOrderNoteResponseDTO;
import com.finetune.app.model.entity.WorkOrder;
import com.finetune.app.model.entity.WorkOrderNote;
import com.finetune.app.repository.WorkOrderNoteRepository;
import com.finetune.app.repository.WorkOrderRepository;
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

    private final WorkOrderNoteRepository workOrderNoteRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderNoteService(
            WorkOrderNoteRepository workOrderNoteRepository,
            WorkOrderRepository workOrderRepository) {
        this.workOrderNoteRepository = workOrderNoteRepository;
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * Get all notes for a work order, ordered by creation date (newest first).
     * 
     * @param workOrderId the work order ID
     * @return list of note DTOs, newest first
     */
    public List<WorkOrderNoteResponseDTO> getNotes(Long workOrderId) {
        List<WorkOrderNote> notes = workOrderNoteRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId);
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
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Work order not found: " + workOrderId));

        // Create note
        WorkOrderNote note = new WorkOrderNote();
        note.setWorkOrder(workOrder);
        note.setNoteText(request.getNoteText());
        note.setCreatedBy(request.getCreatedBy());

        // Save and return DTO
        WorkOrderNote savedNote = workOrderNoteRepository.save(note);
        return WorkOrderNoteResponseDTO.fromEntity(savedNote);
    }
}
