package com.bazarfx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single chat message sent between a buyer and a seller, optionally
 * anchored to the product/listing the conversation is about (a message with
 * no productId is treated as a general conversation between the two users).
 * Persisted in the relational {@code messages} table (see
 * {@link com.bazarfx.db.DatabaseManager}), following the same JDBC pattern
 * already used for orders and reviews.
 */
public class Message implements Serializable {

    private String id;
    private String senderUsername;
    private String receiverUsername;
    private String productId;
    private String productTitle;
    private String content;
    private String createdAt;
    private boolean read;

    public Message() {
    }

    public Message(String senderUsername, String receiverUsername, String productId,
                   String productTitle, String content) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.productId = productId;
        this.productTitle = productTitle;
        this.content = content;
        this.createdAt = LocalDateTime.now().toString();
        this.read = false;
    }

    public String getId() { return id; }
    public String getSenderUsername() { return senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getProductId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }

    // Setters exist so MessageDao can rebuild an exact Message out of a
    // SQLite ResultSet row (same reasoning as Order/Review).
    public void setId(String id) { this.id = id; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { this.read = read; }
}
