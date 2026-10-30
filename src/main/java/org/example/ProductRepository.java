
package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // Produkt in der Datenbank speichern
    public void addProduct(Product product) throws SQLException {

        String sql = """
                INSERT INTO products (id, name, price, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, product.getId());
            statement.setString(2, product.getName());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());

            statement.executeUpdate();
        }
    }

    // Alle Produkte aus der Datenbank lesen
    public List<Product> getAllProducts() throws SQLException {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT id, name, price, quantity FROM products ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Product product = new Product(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getDouble("price"),
                        result.getInt("quantity")
                );

                products.add(product);
            }
        }

        return products;
    }

    // Produkt anhand der ID löschen
    public boolean deleteProduct(int id) throws SQLException {

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    // Produkt aktualisieren
    public boolean updateProduct(Product product) throws SQLException {

        String sql = """
                UPDATE products
                SET name = ?, price = ?, quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantity());
            statement.setInt(4, product.getId());

            return statement.executeUpdate() > 0;
        }
    }
}
