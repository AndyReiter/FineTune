package com.finetune.app.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for signing an agreement on a work order.
 * Requires customer verification via email and phone.
 */

public class SignAgreementRequest {

    @NotBlank(message = "Signature name is required")
    private String signatureName;

    @NotBlank(message = "Email is required for verification")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required for verification")
    private String phone;


    // Optional: base64 PNG image of signature
    private String signatureImageBase64;

    // Constructors
    public SignAgreementRequest() {
    }

    public SignAgreementRequest(String signatureName, String email, String phone, String signatureImageBase64) {
        this.signatureName = signatureName;
        this.email = email;
        this.phone = phone;
        this.signatureImageBase64 = signatureImageBase64;
    }

    // Getters and Setters
        public String getSignatureImageBase64() {
            return signatureImageBase64;
        }

        public void setSignatureImageBase64(String signatureImageBase64) {
            this.signatureImageBase64 = signatureImageBase64;
        }
    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
