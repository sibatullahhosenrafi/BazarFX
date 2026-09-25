package com.bazarfx.service;

import com.bazarfx.db.ReviewDao;
import com.bazarfx.model.Review;

import java.util.List;

/**
 * Reviews now live in the relational {@code reviews} table (see
 * {@link com.bazarfx.db.DatabaseManager}) instead of a JSON file. Every
 * review's {@code order_id} is a FOREIGN KEY back to {@code orders(id)},
 * so a seller's reviews and their underlying orders form a real one-to-many
 * relationship in the database, not just two independent files.
 */
public class ReviewService {

    private final ReviewDao reviewDao = new ReviewDao();

    public void addReview(Review review) {
        reviewDao.insert(review);
    }

    public double getAverageRating(String sellerUsername) {
        List<Review> forSeller = reviewDao.findBySeller(sellerUsername);
        if (forSeller.isEmpty()) return 0.0;
        double total = 0;
        for (Review r : forSeller) {
            total += r.getRating();
        }
        return total / forSeller.size();
    }

    public List<Review> getForSeller(String sellerUsername) {
        return reviewDao.findBySeller(sellerUsername);
    }

    /** True if the given order already has a review attached to it. */
    public boolean hasReview(String orderId) {
        return reviewDao.existsForOrder(orderId);
    }
}
