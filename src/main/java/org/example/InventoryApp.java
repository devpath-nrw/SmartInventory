
package org.example;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class InventoryApp extends Application {

    private final ProductRepository repository = new ProductRepository();
    private final TableView<Product> table = new TableView<>();
    private final Label status = new Label("Bereit");

    @Override
    public void start(Stage stage) {

        Label title = new Label("Smart Inventory Management");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        // Tabelle erstellen
        TableColumn<Product, Integer> idColumn =
                new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Product, String> nameColumn =
                new TableColumn<>("Produktname");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));

        TableColumn<Product, Double> priceColumn =
                new TableColumn<>("Preis (€)");
        priceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));

        TableColumn<Product, Integer> quantityColumn =
                new TableColumn<>("Bestand");
        quantityColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));

        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(priceColumn);
        table.getColumns().add(quantityColumn);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        // Buttons erstellen
        Button addButton = new Button("Produkt hinzufügen");
        Button editButton = new Button("Produkt bearbeiten");
        Button deleteButton = new Button("Produkt löschen");
        Button refreshButton = new Button("Aktualisieren");

        addButton.setOnAction(e -> showProductForm(null));

        editButton.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showWarning("Bitte zuerst ein Produkt auswählen.");
                return;
            }

            showProductForm(selected);
        });

        deleteButton.setOnAction(e -> deleteSelectedProduct());

        refreshButton.setOnAction(e -> loadProducts());

        HBox buttons = new HBox(
                10, addButton, editButton, deleteButton, refreshButton
        );

        VBox layout = new VBox(20, title, buttons, table, status);
        layout.setPadding(new Insets(25));
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 1000, 650);

        stage.setTitle("SmartInventory");
        stage.setScene(scene);
        stage.show();

        loadProducts();
    }

    // Produkte aus PostgreSQL laden
    private void loadProducts() {
        try {
            table.setItems(FXCollections.observableArrayList(
                    repository.getAllProducts()
            ));
            status.setText("Produkte erfolgreich geladen.");

        } catch (SQLException e) {
            showError("Datenbankfehler: " + e.getMessage());
        }
    }

    // Formular zum Hinzufügen und Bearbeiten
    private void showProductForm(Product existing) {

        boolean editing = existing != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Produkt bearbeiten" : "Produkt hinzufügen");

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();

        if (editing) {
            idField.setText(String.valueOf(existing.getId()));
            idField.setDisable(true);

            nameField.setText(existing.getName());
            priceField.setText(String.valueOf(existing.getPrice()));
            quantityField.setText(String.valueOf(existing.getQuantity()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("Produkt-ID:"), idField);
        grid.addRow(1, new Label("Produktname:"), nameField);
        grid.addRow(2, new Label("Preis (€):"), priceField);
        grid.addRow(3, new Label("Bestand:"), quantityField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            double price = Double.parseDouble(
                    priceField.getText().trim().replace(",", ".")
            );
            int quantity = Integer.parseInt(quantityField.getText().trim());

            Product product = new Product(id, name, price, quantity);

            if (editing) {
                boolean updated = repository.updateProduct(product);

                if (!updated) {
                    showWarning("Produkt wurde nicht gefunden.");
                    return;
                }

                status.setText("Produkt erfolgreich bearbeitet.");

            } else {
                repository.addProduct(product);
                status.setText("Produkt erfolgreich hinzugefügt.");
            }

            loadProducts();

        } catch (IllegalArgumentException e) {
            showError("Ungültige Produktdaten: " + e.getMessage());

        } catch (SQLException e) {
            showError("Datenbankfehler: " + e.getMessage());
        }
    }

    // Ausgewähltes Produkt löschen
    private void deleteSelectedProduct() {

        Product selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Bitte zuerst ein Produkt auswählen.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Produkt löschen");
        confirmation.setHeaderText("Produkt wirklich löschen?");
        confirmation.setContentText(selected.getName());

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = repository.deleteProduct(selected.getId());

            if (deleted) {
                loadProducts();
                status.setText("Produkt erfolgreich gelöscht.");
            } else {
                showWarning("Produkt wurde nicht gefunden.");
            }

        } catch (SQLException e) {
            showError("Datenbankfehler: " + e.getMessage());
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        status.setText(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
