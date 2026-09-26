package com.surakshascan.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
            } else {
                properties.load(input);
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
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
            password = properties.getProperty("db.password");
        }
        
        return DriverManager.getConnection(url, user, password);
    }
}
