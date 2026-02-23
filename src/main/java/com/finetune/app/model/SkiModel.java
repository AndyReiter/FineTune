package com.finetune.app.model;

public class SkiModel {
        public void setBrandId(long brandId) {
            if (brand == null) brand = new SkiBrand();
            brand.setId(brandId);
        }
        public Long getBrandId() {
            return brand != null ? brand.getId() : null;
        }
    private Long id;
    private String name;
    private SkiBrand brand;

    public SkiModel() {}

    public SkiModel(String name) {
        this.name = name;
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

    public SkiBrand getBrand() {
        return brand;
    }

    public void setBrand(SkiBrand brand) {
        this.brand = brand;
    }
}
