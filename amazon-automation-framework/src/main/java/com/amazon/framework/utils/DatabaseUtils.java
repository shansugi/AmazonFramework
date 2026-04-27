package com.amazon.framework.utils;

import com.amazon.framework.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * DatabaseUtils — JDBC + HikariCP connection pool for DB validation.
 *
 * WHY HikariCP?
 *   Raw DriverManager.getConnection() creates a new connection per call.
 *   With 10 parallel threads each making DB calls, you'd open 10+ connections
 *   simultaneously and exhaust the DB server's limit.
 *   HikariCP maintains a pool of reusable connections — thread-safe, fast.
 *
 * Usage:
 *   // Verify API-created user exists in DB
 *   assertTrue(DatabaseUtils.recordExists(
 *       "SELECT * FROM users WHERE email=?", "user@test.com"));
 *
 *   // Get order details after checkout
 *   Map<String, Object> order = DatabaseUtils.getRow(
 *       "SELECT * FROM orders WHERE order_id=?", orderId);
 */
public class DatabaseUtils {

    private static final Logger log = LogManager.getLogger(DatabaseUtils.class);
    private static HikariDataSource dataSource;

    static {
        initPool();
    }

    private static synchronized void initPool() {
        if (dataSource != null) return;
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(ConfigManager.get("db.url"));
            config.setUsername(ConfigManager.get("db.username"));
            config.setPassword(ConfigManager.get("db.password"));
            config.setMaximumPoolSize(ConfigManager.getInt("db.pool.size"));
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);
            config.setPoolName("AmazonTestPool");

            dataSource = new HikariDataSource(config);
            log.info("HikariCP pool initialized | maxSize={}",
                     ConfigManager.get("db.pool.size"));
        } catch (Exception e) {
            log.warn("DB pool init failed — DB validation tests will be skipped. Reason: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERY HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Execute a SELECT and return all rows as List of Maps.
     * Each Map: column name → cell value.
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (dataSource == null) return results;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, params);
            log.debug("Executing query: {} | params: {}", sql, Arrays.toString(params));

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }

        log.debug("Query returned {} rows", results.size());
        return results;
    }

    /** Return first row or null if no results. */
    public static Map<String, Object> getRow(String sql, Object... params) {
        List<Map<String, Object>> rows = executeQuery(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Return a single scalar value (e.g. COUNT(*), MAX(id)). */
    public static Object getScalar(String sql, Object... params) {
        Map<String, Object> row = getRow(sql, params);
        if (row == null) return null;
        return row.values().iterator().next();
    }

    /** Return row count matching query. */
    public static int getCount(String sql, Object... params) {
        Object scalar = getScalar(sql, params);
        return scalar == null ? 0 : ((Number) scalar).intValue();
    }

    /** True if at least one row matches the query. */
    public static boolean recordExists(String sql, Object... params) {
        return !executeQuery(sql, params).isEmpty();
    }

    /** Execute INSERT / UPDATE / DELETE. Returns rows affected. */
    public static int executeUpdate(String sql, Object... params) {
        if (dataSource == null) return 0;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            int rows = ps.executeUpdate();
            log.debug("Update affected {} rows: {}", rows, sql);
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + sql, e);
        }
    }

    /** Clean up test data after a test run. */
    public static void deleteTestData(String table, String whereColumn, Object value) {
        String sql = "DELETE FROM " + table + " WHERE " + whereColumn + " = ?";
        int deleted = executeUpdate(sql, value);
        log.info("Cleaned up {} row(s) from {} where {}={}", deleted, table, whereColumn, value);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMMON AMAZON-SPECIFIC VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /** Verify an order exists in DB after checkout. */
    public static boolean orderExistsInDB(String orderId) {
        return recordExists(
            "SELECT 1 FROM orders WHERE order_id = ?", orderId);
    }

    /** Get order status from DB. */
    public static String getOrderStatusFromDB(String orderId) {
        Object status = getScalar(
            "SELECT status FROM orders WHERE order_id = ?", orderId);
        return status == null ? null : status.toString();
    }

    /** Verify user session after login. */
    public static boolean userSessionExists(String email) {
        return recordExists(
            "SELECT 1 FROM user_sessions WHERE email = ? AND active = 1", email);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────────────────────────────
    private static void setParams(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP pool closed");
        }
    }
}
