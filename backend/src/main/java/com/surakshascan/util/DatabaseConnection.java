package com.surakshascan.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("[DB] Failed to load application.properties or JDBC driver: " + ex.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        // Environment variables take priority over application.properties (production-safe)
        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = properties.getProperty("db.url");
        }

        String user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = properties.getProperty("db.username");
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = properties.getProperty("db.password", "");
        }

        // Safe diagnostic log — never prints password or full URL
        if (url != null) {
            try {
                java.net.URI parsed = new java.net.URI(url.replace("jdbc:mysql://", "mysql://"));
                System.out.println("[DB] Connecting to host=" + parsed.getHost()
                        + " port=" + parsed.getPort()
                        + " path=" + parsed.getPath()
                        + " ssl=" + (url.contains("sslMode") || url.contains("useSSL")));
            } catch (Exception e) {
                System.out.println("[DB] Connecting (URL parse failed for diagnostics): " + e.getMessage());
            }
        } else {
            System.err.println("[DB] ERROR: DB_URL is null — check Render environment variables.");
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("[DB] Connection established successfully.");
            return conn;
        } catch (SQLException e) {
            // Log the error class and message but NOT the URL (which may contain credentials)
            System.err.println("[DB] Connection FAILED — SQLState=" + e.getSQLState()
                    + " ErrorCode=" + e.getErrorCode()
                    + " Message=" + e.getMessage());
            throw e;
        }
    }
}
