

package org.example;

import java.sql.SQLException;

public class InventoryManager {

    private final ProductRepository repository;

    public InventoryManager() {
        repository = new ProductRepository();
    }

    // Produkt hinzufügen
    public void addProduct(Product product) {
        try {
            if (findProduct(product.getId()) != null) {
                System.out.println("Diese ID existiert bereits!");
                return;
            }

            repository.addProduct(product);
            System.out.println("Produkt gespeichert: " + product.getName());

        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }

    // Alle Produkte anzeigen
    public void showAllProducts() {
        try {
            var products = repository.getAllProducts();

            if (products.isEmpty()) {
                System.out.println("Lager ist leer.");
                return;
            }

            for (Product product : products) {
                product.displayInfo();
            }

        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }

    // Produkt suchen
    public Product findProduct(int id) throws SQLException {
        for (Product product : repository.getAllProducts()) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    // Produkt löschen
    public void removeProduct(int id) {
        try {
            if (repository.deleteProduct(id)) {
                System.out.println("Produkt gelöscht.");
            } else {
                System.out.println("Produkt nicht gefunden.");
            }

        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }

    public void updateProduct(int id, double price, int quantity) {
        try {
            Product product = findProduct(id);

            if (product == null) {
                System.out.println("Produkt nicht gefunden.");
                return;
            }

            if (price < 0 || quantity < 0) {
                System.out.println("Ungültiger Preis oder Bestand.");
                return;
            }

            product.setPrice(price);
            product.setQuantity(quantity);

            if (repository.updateProduct(product)) {
                System.out.println("Produkt erfolgreich aktualisiert!");
            }

        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }
}
