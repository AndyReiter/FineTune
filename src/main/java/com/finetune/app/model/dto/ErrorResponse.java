package com.finetune.app.model.dto;

/**
 * DTO for error responses.
 */
public class ErrorResponse {
    
    private boolean error;
    private String message;

    public ErrorResponse() {
        this.error = true;
    }

    public ErrorResponse(String message) {
        this.error = true;
        this.message = message;
    }

    public ErrorResponse(boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    // Getters and Setters
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
