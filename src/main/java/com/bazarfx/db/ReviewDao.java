package com.bazarfx.db;

import com.bazarfx.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code reviews} table - the child side of the
 * orders/reviews relationship (each review's {@code order_id} is a FOREIGN
 * KEY back to {@code orders(id)}). Same PreparedStatement + CRUD pattern as
 * {@link OrderDao}: INSERT ({@link #insert}), DELETE ({@link #delete}), and
 * SELECT ({@link #findAll}, {@link #findBySeller}, {@link #findReviewSummariesForOrder}).
 */
public class ReviewDao {

    public void insert(Review review) {
        String sql = "INSERT INTO reviews (id, order_id, seller_username, buyer_username, " +
                "rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getId());
            ps.setString(2, review.getOrderId());
            ps.setString(3, review.getSellerUsername());
            ps.setString(4, review.getBuyerUsername());
            ps.setInt(5, review.getRating());
            ps.setString(6, review.getComment());
            ps.setString(7, review.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDao.insert failed for review " + review.getId() + ": " + e.getMessage());
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("ReviewDao.delete failed for review " + id + ": " + e.getMessage());
        }
    }

    public List<Review> findAll() {
        String sql = "SELECT * FROM reviews ORDER BY created_at DESC";
        List<Review> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("ReviewDao.findAll failed: " + e.getMessage());
        }
        return results;
    }

    /** All reviews left for a given seller - powers the seller's average rating. */
    public List<Review> findBySeller(String sellerUsername) {
        String sql = "SELECT * FROM reviews WHERE seller_username = ? ORDER BY created_at DESC";
        List<Review> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ReviewDao.findBySeller failed: " + e.getMessage());
        }
        return results;
    }

    /**
     * A join across the relationship: pulls each review together with the
     * order it belongs to, purely to demonstrate the orders/reviews FK link
     * in a single query (orders.product_title travels along with the review).
     */
    public List<String> findReviewSummariesForOrder(String orderId) {
        String sql = "SELECT r.rating, r.comment, o.product_title " +
                "FROM reviews r JOIN orders o ON r.order_id = o.id " +
                "WHERE r.order_id = ?";
        List<String> summaries = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summaries.add(rs.getInt("rating") + "/5 on \"" + rs.getString("product_title") +
                            "\": " + rs.getString("comment"));
                }
            }
        } catch (SQLException e) {
            System.err.println("ReviewDao.findReviewSummariesForOrder failed: " + e.getMessage());
        }
        return summaries;
    }

    public boolean existsForOrder(String orderId) {
        String sql = "SELECT COUNT(*) AS cnt FROM reviews WHERE order_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            System.err.println("ReviewDao.existsForOrder failed: " + e.getMessage());
            return false;
        }
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getString("id"));
        review.setOrderId(rs.getString("order_id"));
        review.setSellerUsername(rs.getString("seller_username"));
        review.setBuyerUsername(rs.getString("buyer_username"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getString("created_at"));
        return review;
    }
}
