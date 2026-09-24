package com.bazarfx.service;

import com.bazarfx.model.Review;
import com.bazarfx.util.JsonStorage;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReviewService {

    private static final String FILE = "reviews.json";
    private final List<Review> reviews = new CopyOnWriteArrayList<>();

    public ReviewService() {
        reviews.addAll(JsonStorage.<Review>loadList(FILE, new TypeToken<List<Review>>() {}.getType()));
    }

    public void addReview(Review review) {
        reviews.add(review);
        persist();
    }

    public double getAverageRating(String sellerUsername) {
        double total = 0;
        int count = 0;
        for (Review r : reviews) {
            if (r.getSellerUsername().equals(sellerUsername)) {
                total += r.getRating();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    public List<Review> getForSeller(String sellerUsername) {
        List<Review> result = new ArrayList<>();
        for (Review r : reviews) {
            if (r.getSellerUsername().equals(sellerUsername)) result.add(r);
        }
        return result;
    }

    private void persist() {
        JsonStorage.saveList(FILE, new ArrayList<>(reviews));
    }
}
