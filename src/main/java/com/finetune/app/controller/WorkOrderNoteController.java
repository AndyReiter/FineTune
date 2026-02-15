package com.finetune.app.controller;

import com.finetune.app.model.dto.CreateWorkOrderNoteRequest;
import com.finetune.app.model.dto.WorkOrderNoteResponseDTO;
import com.finetune.app.service.WorkOrderNoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing work order notes.
 * Simple append-only notes for staff use.
 * 
 * Endpoints:
 * - GET /api/workorders/{workOrderId}/notes - Get all notes for a work order
 * - POST /api/workorders/{workOrderId}/notes - Add a new note to a work order
 */
@RestController
@RequestMapping("/api/workorders")
@CrossOrigin(origins = "*")
public class WorkOrderNoteController {

    private final WorkOrderNoteService workOrderNoteService;

    public WorkOrderNoteController(WorkOrderNoteService workOrderNoteService) {
        this.workOrderNoteService = workOrderNoteService;
    }

    /**
     * Get all notes for a work order, ordered by creation date (newest first).
     * 
     * @param workOrderId the work order ID
     * @return list of notes ordered by creation date descending
     */
    @GetMapping("/{workOrderId}/notes")
    public ResponseEntity<List<WorkOrderNoteResponseDTO>> getNotes(@PathVariable Long workOrderId) {
        List<WorkOrderNoteResponseDTO> notes = workOrderNoteService.getNotes(workOrderId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Add a new note to a work order.
     * 
     * @param workOrderId the work order ID
     * @param request the note creation request containing noteText and createdBy
     * @return the created note with generated ID and timestamp
     */
    @PostMapping("/{workOrderId}/notes")
    public ResponseEntity<WorkOrderNoteResponseDTO> addNote(
            @PathVariable Long workOrderId,
            @Valid @RequestBody CreateWorkOrderNoteRequest request) {
        WorkOrderNoteResponseDTO note = workOrderNoteService.addNote(workOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }
}
