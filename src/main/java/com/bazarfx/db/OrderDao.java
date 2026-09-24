package com.bazarfx.db;

import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code orders} table - the project's hands-on
 * demonstration of the four core SQL operations required by the syllabus:
 * INSERT ({@link #insert}), UPDATE ({@link #update}), DELETE ({@link #delete}),
 * and SELECT ({@link #findAll}, {@link #findByBuyer}, {@link #findById}).
 *
 * Every method opens and closes its own short-lived JDBC connection
 * (try-with-resources), which is the simplest correct pattern for a
 * small desktop app talking to a local SQLite file.
 */
public class OrderDao {

    public void insert(Order order) {
        String sql = "INSERT INTO orders (id, buyer_username, product_id, product_title, " +
                "quantity, total_price, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getId());
            ps.setString(2, order.getBuyerUsername());
            ps.setString(3, order.getProductId());
            ps.setString(4, order.getProductTitle());
            ps.setInt(5, order.getQuantity());
            ps.setDouble(6, order.getTotalPrice());
            ps.setString(7, order.getStatus().name());
            ps.setString(8, order.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("OrderDao.insert failed for order " + order.getId() + ": " + e.getMessage());
        }
    }

    /** Updates just the mutable field (status) of an already-inserted order. */
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getStatus().name());
            ps.setString(2, order.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("OrderDao.update failed for order " + order.getId() + ": " + e.getMessage());
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("OrderDao.delete failed for order " + id + ": " + e.getMessage());
        }
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        List<Order> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("OrderDao.findAll failed: " + e.getMessage());
        }
        return results;
    }

    public List<Order> findByBuyer(String username) {
        String sql = "SELECT * FROM orders WHERE buyer_username = ? ORDER BY created_at DESC";
        List<Order> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("OrderDao.findByBuyer failed: " + e.getMessage());
        }
        return results;
    }

    public Order findById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("OrderDao.findById failed: " + e.getMessage());
        }
        return null;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getString("id"));
        order.setBuyerUsername(rs.getString("buyer_username"));
        order.setProductId(rs.getString("product_id"));
        order.setProductTitle(rs.getString("product_title"));
        order.setQuantity(rs.getInt("quantity"));
        order.setTotalPrice(rs.getDouble("total_price"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setCreatedAt(rs.getString("created_at"));
        return order;
    }
}
