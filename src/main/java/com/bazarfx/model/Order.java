package com.bazarfx.model;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.UUID;

/** A simulated purchase. No real payment is processed. */
public class Order implements Serializable {

    private String id;
    private String buyerUsername;
    private String productId;
    private String productTitle;
    private int quantity;
    private double totalPrice;
    private volatile OrderStatus status;
    private String createdAt;

    public Order() {
    }

    public Order(String buyerUsername, String productId, String productTitle, int quantity, double totalPrice) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.buyerUsername = buyerUsername;
        this.productId = productId;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public String getBuyerUsername() { return buyerUsername; }
    public String getProductId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    /** Mutated only from OrderStatusSimulator's background thread. */
    public synchronized void setStatus(OrderStatus status) { this.status = status; }

    // Setters below exist only so OrderDao can rebuild an exact Order object
    // (same id/status/createdAt) out of a SQLite ResultSet row.
    public void setId(String id) { this.id = id; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
