package com.bazarfx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A single marketplace listing created by a seller. */
public class Product implements Serializable {

    private String id;
    private String sellerUsername;
    private String title;
    private String description;
    private String category;
    private double price;
    private String condition;   // "New" or "Used"
    private String location;
    private List<String> imagePaths = new ArrayList<>();
    private int views;
    private boolean active;
    private String createdAt;

    public Product() {
    }

    public Product(String sellerUsername, String title, String description, String category,
                    double price, String condition, String location) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.sellerUsername = sellerUsername;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.active = true;
        this.views = 0;
        this.createdAt = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public String getSellerUsername() { return sellerUsername; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public String getLocation() { return location; }
    public List<String> getImagePaths() { return imagePaths; }
    public int getViews() { return views; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }

    public void addImagePath(String path) { this.imagePaths.add(path); }
    public void incrementViews() { this.views++; }
    public void setActive(boolean active) { this.active = active; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return title + "  -  BDT " + price;
    }
}
