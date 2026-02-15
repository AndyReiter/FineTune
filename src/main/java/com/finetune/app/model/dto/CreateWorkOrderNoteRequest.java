package com.finetune.app.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new work order note.
 * Staff-only append-only notes.
 */
public class CreateWorkOrderNoteRequest {

    @NotBlank(message = "Note text is required")
    private String noteText;

    @NotBlank(message = "Creator name is required")
    private String createdBy;

    // Constructors
    public CreateWorkOrderNoteRequest() {
    }

    public CreateWorkOrderNoteRequest(String noteText, String createdBy) {
        this.noteText = noteText;
        this.createdBy = createdBy;
    }

    // Getters and Setters
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
}
