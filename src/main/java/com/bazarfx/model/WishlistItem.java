package com.bazarfx.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * One (username, productId) pairing - a single saved listing on a single
 * user's wishlist. Kept as a flat list in wishlist.json via
 * {@link com.bazarfx.util.JsonStorage}, the same file-backed pattern already
 * used for users.json and products.json.
 */
public class WishlistItem implements Serializable {

    private String username;
    private String productId;
    private String addedAt;

    public WishlistItem() {
    }

    public WishlistItem(String username, String productId) {
        this.username = username;
        this.productId = productId;
        this.addedAt = LocalDateTime.now().toString();
    }

    public String getUsername() { return username; }
    public String getProductId() { return productId; }
    public String getAddedAt() { return addedAt; }
}
