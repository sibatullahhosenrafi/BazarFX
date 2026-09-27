package com.bazarfx.service;

import com.bazarfx.model.WishlistItem;
import com.bazarfx.util.JsonStorage;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class WishlistService {

    private static final String FILE = "wishlist.json";
    private final List<WishlistItem> items;

    public WishlistService() {
        items = new CopyOnWriteArrayList<>(
                JsonStorage.loadList(FILE, new TypeToken<List<WishlistItem>>() {}.getType()));
    }

    public boolean isWishlisted(String username, String productId) {
        for (WishlistItem item : items) {
            if (item.getUsername().equals(username) && item.getProductId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    public void add(String username, String productId) {
        if (!isWishlisted(username, productId)) {
            items.add(new WishlistItem(username, productId));
            persist();
        }
    }

    public void remove(String username, String productId) {
        items.removeIf(item -> item.getUsername().equals(username) && item.getProductId().equals(productId));
        persist();
    }

    public boolean toggle(String username, String productId) {
        if (isWishlisted(username, productId)) {
            remove(username, productId);
            return false;
        } else {
            add(username, productId);
            return true;
        }
    }

    public List<String> getProductIdsForUser(String username) {
        List<String> ids = new ArrayList<>();
        for (WishlistItem item : items) {
            if (item.getUsername().equals(username)) ids.add(item.getProductId());
        }
        return ids;
    }

    public synchronized void persist() {
        JsonStorage.saveList(FILE, new ArrayList<>(items));
    }
}
