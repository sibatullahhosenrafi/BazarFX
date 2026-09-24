package com.bazarfx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Review implements Serializable {
    private String id;
    private String orderId;
    private String sellerUsername;
    private String buyerUsername;
    private int rating; // 1-5
    private String comment;
    private String createdAt;

    public Review() {
    }

    public Review(String orderId, String sellerUsername, String buyerUsername, int rating, String comment) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.orderId = orderId;
        this.sellerUsername = sellerUsername;
        this.buyerUsername = buyerUsername;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public String getSellerUsername() { return sellerUsername; }
    public String getBuyerUsername() { return buyerUsername; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}
