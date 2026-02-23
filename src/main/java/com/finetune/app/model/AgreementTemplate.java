package com.finetune.app.model;
import java.util.List;

public class AgreementTemplate {
    private java.time.LocalDateTime createdAt;
    public void setShopId(long shopId) {
        if (shop == null) shop = new Shop();
        shop.setId(shopId);
    }
    public Long getShopId() {
        return shop != null ? shop.getId() : null;
    }
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private Long id;

    private String title;
    private String agreementText;
    private String logoUrl;
    private String jurisdictionState;
    private boolean isActive;
    private Shop shop;
    private List<SignedAgreement> signedAgreements;

    public AgreementTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAgreementText() { return agreementText; }
    public void setAgreementText(String agreementText) { this.agreementText = agreementText; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getJurisdictionState() { return jurisdictionState; }
    public void setJurisdictionState(String jurisdictionState) { this.jurisdictionState = jurisdictionState; }
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

    public List<SignedAgreement> getSignedAgreements() { return signedAgreements; }
    public void setSignedAgreements(List<SignedAgreement> signedAgreements) { this.signedAgreements = signedAgreements; }
}
