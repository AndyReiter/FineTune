package com.finetune.app.model.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for agreement signing operation.
 */
public class SignAgreementResponse {

    private boolean success;
    private String message;
    private String agreementId;
    private String pdfUrl;
    private LocalDateTime signedAt;
    private Long workOrderId;

    // Constructors
    public SignAgreementResponse() {
    }

    public SignAgreementResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Builder pattern for easier construction
    public static SignAgreementResponse success(String agreementId, String pdfUrl, 
                                                LocalDateTime signedAt, Long workOrderId) {
        SignAgreementResponse response = new SignAgreementResponse();
        response.success = true;
        response.message = "Agreement signed successfully";
        response.agreementId = agreementId;
        response.pdfUrl = pdfUrl;
        response.signedAt = signedAt;
        response.workOrderId = workOrderId;
        return response;
    }

    public static SignAgreementResponse error(String message) {
        return new SignAgreementResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }
}
