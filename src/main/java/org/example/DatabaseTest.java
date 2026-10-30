package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println("Verbindung erfolgreich!");
            System.out.println("Datenbank: smart_inventory");

        } catch (SQLException e) {
            System.out.println("Verbindung fehlgeschlagen!");
            System.out.println(e.getMessage());
        }
    }
}