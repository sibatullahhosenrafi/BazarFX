package com.bazarfx.db;

import com.bazarfx.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for the {@code messages} table - the in-app
 * buyer/seller chat feature. Same try-with-resources + PreparedStatement
 * pattern as {@link OrderDao} and {@link ReviewDao}: INSERT ({@link #insert}),
 * UPDATE ({@link #markRead}), and SELECT ({@link #findConversation},
 * {@link #findAllForUser}, {@link #countUnread}).
 */
public class MessageDao {

    public void insert(Message message) {
        String sql = "INSERT INTO messages (id, sender_username, receiver_username, product_id, " +
                "product_title, content, created_at, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message.getId());
            ps.setString(2, message.getSenderUsername());
            ps.setString(3, message.getReceiverUsername());
            ps.setString(4, message.getProductId());
            ps.setString(5, message.getProductTitle());
            ps.setString(6, message.getContent());
            ps.setString(7, message.getCreatedAt());
            ps.setInt(8, message.isRead() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("MessageDao.insert failed for message " + message.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Every message exchanged between userA and userB about one specific
     * product (or general conversation, when productId is null), oldest first
     * so the chat window can render top-to-bottom in send order.
     */
    public List<Message> findConversation(String userA, String userB, String productId) {
        String sql = "SELECT * FROM messages " +
                "WHERE ((sender_username = ? AND receiver_username = ?) " +
                "    OR (sender_username = ? AND receiver_username = ?)) " +
                "AND ((product_id IS NULL AND ? IS NULL) OR product_id = ?) " +
                "ORDER BY created_at ASC";
        List<Message> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userA);
            ps.setString(2, userB);
            ps.setString(3, userB);
            ps.setString(4, userA);
            ps.setString(5, productId);
            ps.setString(6, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("MessageDao.findConversation failed: " + e.getMessage());
        }
        return results;
    }

    /** Every message the user has sent or received, newest first - used to build the conversation list. */
    public List<Message> findAllForUser(String username) {
        String sql = "SELECT * FROM messages WHERE sender_username = ? OR receiver_username = ? " +
                "ORDER BY created_at DESC";
        List<Message> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("MessageDao.findAllForUser failed: " + e.getMessage());
        }
        return results;
    }

    /** Marks every message FROM otherUsername TO me (about this product) as read. */
    public void markRead(String me, String otherUsername, String productId) {
        String sql = "UPDATE messages SET is_read = 1 " +
                "WHERE receiver_username = ? AND sender_username = ? " +
                "AND ((product_id IS NULL AND ? IS NULL) OR product_id = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, me);
            ps.setString(2, otherUsername);
            ps.setString(3, productId);
            ps.setString(4, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("MessageDao.markRead failed: " + e.getMessage());
        }
    }

    /** Total unread messages waiting for this user, across every conversation - powers the sidebar badge. */
    public int countUnread(String username) {
        String sql = "SELECT COUNT(*) AS cnt FROM messages WHERE receiver_username = ? AND is_read = 0";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("MessageDao.countUnread failed: " + e.getMessage());
        }
        return 0;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getString("id"));
        message.setSenderUsername(rs.getString("sender_username"));
        message.setReceiverUsername(rs.getString("receiver_username"));
        message.setProductId(rs.getString("product_id"));
        message.setProductTitle(rs.getString("product_title"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getString("created_at"));
        message.setRead(rs.getInt("is_read") != 0);
        return message;
    }
}
