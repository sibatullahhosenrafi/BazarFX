package com.bazarfx.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Week 6 - Relational Database with SQLite and JavaFX.
 *
 * Central JDBC access point for BazarFX's relational storage. Two tables
 * live here with a real one-to-many relationship between them:
 *   - orders: inserted at checkout, updated as status changes, queried by
 *     the dashboard/report screens, deleted on cancellation.
 *   - reviews: a buyer leaves at most one review per order, so each review
 *     row stores order_id as a FOREIGN KEY back to orders(id) - that's the
 *     "relationship between tables" the syllabus asks for. Users/Products
 *     remain on the original JSON file storage (see
 *     {@link com.bazarfx.util.JsonStorage}) so both approaches are visible
 *     side by side in the same project.
 *
 * The database file is created automatically on first use at
 * data/bazarfx.db - no manual setup required.
 */
public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/bazarfx.db";
    private static volatile boolean schemaReady = false;

    private DatabaseManager() {
    }

    /** Opens a fresh JDBC connection, creating the schema first if this is the first call. */
    public static synchronized Connection getConnection() {
        try {
            if (!schemaReady) {
                createDataDirectory();
                createSchema();
                schemaReady = true;
            }
            Connection conn = DriverManager.getConnection(DB_URL);
            // Foreign keys are OFF by default in SQLite - must be enabled per connection.
            try (Statement pragma = conn.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Could not open SQLite database at " + DB_URL, e);
        }
    }

    private static void createDataDirectory() {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory for SQLite database", e);
        }
    }

    /** Table creation - run once, guarded by CREATE TABLE IF NOT EXISTS so re-runs are safe. */
    private static void createSchema() throws SQLException {
        String createOrdersTable =
                "CREATE TABLE IF NOT EXISTS orders (" +
                        "id TEXT PRIMARY KEY, " +
                        "buyer_username TEXT NOT NULL, " +
                        "product_id TEXT NOT NULL, " +
                        "product_title TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "total_price REAL NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "created_at TEXT NOT NULL" +
                        ")";

        // Child table: every review belongs to exactly one order, enforced with
        // a FOREIGN KEY. Deleting an order cascades to its review.
        String createReviewsTable =
                "CREATE TABLE IF NOT EXISTS reviews (" +
                        "id TEXT PRIMARY KEY, " +
                        "order_id TEXT NOT NULL, " +
                        "seller_username TEXT NOT NULL, " +
                        "buyer_username TEXT NOT NULL, " +
                        "rating INTEGER NOT NULL, " +
                        "comment TEXT, " +
                        "created_at TEXT NOT NULL, " +
                        "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE" +
                        ")";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(createOrdersTable);
            stmt.execute(createReviewsTable);
        }
    }
}
