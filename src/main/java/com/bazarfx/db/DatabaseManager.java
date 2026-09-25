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
 * Central JDBC access point for BazarFX's relational storage. Orders are the
 * entity chosen to live in a real relational table (they have a natural
 * lifecycle - inserted at checkout, updated as their status changes, queried
 * for the dashboard/report screens, and deleted on cancellation) while
 * Users/Products/Reviews remain on the original JSON file storage
 * (see {@link com.bazarfx.util.JsonStorage}) so both approaches from the
 * syllabus are visible side by side in the same project.
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
            return DriverManager.getConnection(DB_URL);
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

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createOrdersTable);
        }
    }
}
