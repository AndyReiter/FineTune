package com.finetune.app.model;

import java.time.LocalDateTime;

public class SignedAgreement {
                public String getStorageKey() {
                    return pdfStorageKey;
                }
            // Add id setters/getters for repository compatibility
            public Long getWorkOrderId() {
                return workOrder != null ? workOrder.getId() : null;
            }
            public void setWorkOrderId(Long workOrderId) {
                if (workOrder == null) workOrder = new WorkOrder();
                workOrder.setId(workOrderId);
            }
            public Long getCustomerId() {
                return customer != null ? customer.getId() : null;
            }
            public void setCustomerId(Long customerId) {
                if (customer == null) customer = new Customer();
                customer.setId(customerId);
            }
            public String getAgreementTemplateId() {
                return agreementTemplate != null ? agreementTemplate.getId().toString() : null;
            }
            public void setAgreementTemplateId(String agreementTemplateId) {
                if (agreementTemplate == null) agreementTemplate = new AgreementTemplate();
                agreementTemplate.setId(Long.valueOf(agreementTemplateId));
            }
        public SignedAgreement() {}

        public SignedAgreement(WorkOrder workOrder, Customer customer, AgreementTemplate agreementTemplate, String pdfStorageKey, String signatureName, LocalDateTime signedAt, String documentHash) {
            this.workOrder = workOrder;
            this.customer = customer;
            this.agreementTemplate = agreementTemplate;
            this.pdfStorageKey = pdfStorageKey;
            this.signatureName = signatureName;
            this.signedAt = signedAt;
            this.documentHash = documentHash;
        }
    private String id;
    private WorkOrder workOrder;
    private Customer customer;
    private AgreementTemplate agreementTemplate;
    private String pdfStorageKey;
    private String signatureName;
    private String signatureIp;
    private String signatureUserAgent;
    private LocalDateTime signedAt;
    private String documentHash;
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public AgreementTemplate getAgreementTemplate() {
        return agreementTemplate;
    }

    public void setAgreementTemplate(AgreementTemplate agreementTemplate) {
        this.agreementTemplate = agreementTemplate;
    }

    public String getPdfStorageKey() {
        return pdfStorageKey;
    }

    public void setPdfStorageKey(String pdfStorageKey) {
        this.pdfStorageKey = pdfStorageKey;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getSignatureIp() {
        return signatureIp;
    }

    public void setSignatureIp(String signatureIp) {
        this.signatureIp = signatureIp;
    }

    public String getSignatureUserAgent() {
        return signatureUserAgent;
    }

    public void setSignatureUserAgent(String signatureUserAgent) {
        this.signatureUserAgent = signatureUserAgent;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getDocumentHash() {
        return documentHash;
    }

    public void setDocumentHash(String documentHash) {
        this.documentHash = documentHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}