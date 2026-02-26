package com.finetune.app.model;

import java.time.LocalDateTime;

/**
 * Staff domain model for authentication and authorization.
 */
public class Staff {

    private Long id;

    private String email;

    private String password;

    private String role = "ROLE_STAFF";

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private java.util.List<com.finetune.app.model.Shop> shops;

    // Remove JPA lifecycle methods; set timestamps manually in service/repository if needed

    // Constructors
    public Staff() {}

    public Staff(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.util.List<com.finetune.app.model.Shop> getShops() {
        return shops;
    }

    public void setShops(java.util.List<com.finetune.app.model.Shop> shops) {
        this.shops = shops;
    }

    public java.util.List<Long> getShopIds() {
        if (shops == null) return java.util.List.of();
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (com.finetune.app.model.Shop s : shops) {
            if (s != null && s.getId() != null) ids.add(s.getId());
        }
        return ids;
    }
}