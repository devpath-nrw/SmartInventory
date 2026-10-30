
package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/smart_inventory";

    private static final String USER = "postgres";

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new SQLException("DB_PASSWORD ist nicht gesetzt.");
        }

        return DriverManager.getConnection(URL, USER, password);
    }
}

