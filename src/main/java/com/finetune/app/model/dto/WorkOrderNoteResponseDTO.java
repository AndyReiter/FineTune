package com.finetune.app.model.dto;

import com.finetune.app.model.entity.WorkOrderNote;
import java.time.LocalDateTime;

/**
 * DTO for WorkOrderNote responses.
 * Returns note data without exposing internal entity structure.
 */
public class WorkOrderNoteResponseDTO {

    private Long id;
    private String noteText;
    private String createdBy;
    private LocalDateTime createdAt;

    // Constructors
    public WorkOrderNoteResponseDTO() {
    }

    public WorkOrderNoteResponseDTO(Long id, String noteText, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.noteText = noteText;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /**
     * Factory method to convert a WorkOrderNote entity to this DTO.
     */
    public static WorkOrderNoteResponseDTO fromEntity(WorkOrderNote note) {
        WorkOrderNoteResponseDTO dto = new WorkOrderNoteResponseDTO();
        dto.id = note.getId();
        dto.noteText = note.getNoteText();
        dto.createdBy = note.getCreatedBy();
        dto.createdAt = note.getCreatedAt();
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
