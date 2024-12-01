package com.billy.backend.dto;

public class ProductResponse {
    private String name;
    private String slug;
    private String description;
    private Float price;
    private Integer quantity;

    public ProductResponse(String name, String slug, String description, Float price, Integer quantity) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Float getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
