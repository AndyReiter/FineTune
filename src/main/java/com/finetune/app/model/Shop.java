package com.finetune.app.model;
import com.fasterxml.jackson.annotation.JsonBackReference;


public class Shop {
        public void setLocationId(long locationId) {
            if (location == null) location = new Location();
            location.setId(locationId);
        }
        public Long getLocationId() {
            return location != null ? location.getId() : null;
        }
    private Long id;
    private String name;
    private String status;
    private String logoUrl;
    private Location location;

    public Shop() {}

    public Shop(Long id, String name, String status, Location location) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.location = location;
        this.logoUrl = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
