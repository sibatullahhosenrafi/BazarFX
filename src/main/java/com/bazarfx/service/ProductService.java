package com.bazarfx.service;

import com.bazarfx.model.Product;
import com.bazarfx.util.JsonStorage;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory product cache backed by products.json.
 * ConcurrentHashMap is used because the UI thread reads it while background
 * threads (image processing, report generation) may read/write concurrently.
 */
public class ProductService {

    private static final String FILE = "products.json";
    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();

    public ProductService() {
        List<Product> loaded = JsonStorage.loadList(FILE, new TypeToken<List<Product>>() {}.getType());
        for (Product p : loaded) {
            products.put(p.getId(), p);
        }
    }

    public void addProduct(Product product) {
        products.put(product.getId(), product);
        persist();
    }

    public Product getById(String id) {
        return products.get(id);
    }

    public List<Product> getAllActive() {
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.isActive()) result.add(p);
        }
        return result;
    }

    public List<Product> search(String keyword, String category) {
        List<Product> result = new ArrayList<>();
        String kw = keyword == null ? "" : keyword.toLowerCase();
        for (Product p : products.values()) {
            if (!p.isActive()) continue;
            boolean matchesKeyword = kw.isBlank()
                    || p.getTitle().toLowerCase().contains(kw)
                    || p.getDescription().toLowerCase().contains(kw);
            boolean matchesCategory = category == null || category.equals("All") || category.equals(p.getCategory());
            if (matchesKeyword && matchesCategory) result.add(p);
        }
        return result;
    }

    public void incrementViews(String productId) {
        Product p = products.get(productId);
        if (p != null) {
            p.incrementViews();
            persist();
        }
    }

    public List<Product> getBySeller(String username) {
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.getSellerUsername().equals(username)) result.add(p);
        }
        return result;
    }

    /** Snapshot of all products, active or not - used by ReportGenerator. */
    public List<Product> getAllSnapshot() {
        return new ArrayList<>(products.values());
    }

    public synchronized void persist() {
        JsonStorage.saveList(FILE, new ArrayList<>(products.values()));
    }
}
