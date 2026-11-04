
package org.example;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    private final Label productsCard = new Label("Produkte: 0");
    private final Label stockCard = new Label("Lagerbestand: 0");
    private final Label valueCard = new Label("Lagerwert: 0,00 €");
    private final TextField searchField = new TextField();
    private final javafx.collections.ObservableList<Product> allProducts =
            FXCollections.observableArrayList();
    private final FilteredList<Product> filteredProducts =
            new FilteredList<>(allProducts, product -> true);


    @Override
    public void start(Stage stage) {

        Label title = new Label("Smart Inventory Management");
        title.getStyleClass().add("title-label");


        VBox card1 = new VBox(8, new Label("Produkte"), productsCard);
        VBox card2 = new VBox(8, new Label("Lagerbestand"), stockCard);
        VBox card3 = new VBox(8, new Label("Lagerwert"), valueCard);

        card1.getStyleClass().add("stat-card");
        card2.getStyleClass().add("stat-card");
        card3.getStyleClass().add("stat-card");

        productsCard.getStyleClass().add("stat-value");
        stockCard.getStyleClass().add("stat-value");
        valueCard.getStyleClass().add("stat-value");


        HBox dashboard = new HBox(15, card1, card2, card3);
        dashboard.getStyleClass().add("dashboard");


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
        table.getStyleClass().add("product-table");


        // Buttons erstellen
        Button addButton = new Button("Produkt hinzufügen");
        Button editButton = new Button("Produkt bearbeiten");
        Button deleteButton = new Button("Produkt löschen");
        Button refreshButton = new Button("Aktualisieren");

        addButton.getStyleClass().add("primary-button");
        editButton.getStyleClass().add("secondary-button");
        deleteButton.getStyleClass().add("danger-button");
        refreshButton.getStyleClass().add("refresh-button");


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
        buttons.getStyleClass().add("action-bar");

// Suchfeld konfigurieren
        searchField.setPromptText("Produkt suchen...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        filteredProducts.setPredicate(product -> true);
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    String search = newValue.toLowerCase().trim();

                    filteredProducts.setPredicate(product ->
                            product.getName().toLowerCase().contains(search)
                                    || String.valueOf(product.getId()).contains(search)
                    );
                }
        );

        SortedList<Product> sortedProducts =
                new SortedList<>(filteredProducts);

        sortedProducts.comparatorProperty().bind(
                table.comparatorProperty()
        );

        table.setItems(sortedProducts);

        VBox layout = new VBox(20, title, dashboard, buttons, searchField, table, status);

        layout.setPadding(new Insets(25));
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 1000, 650);


        scene.getStylesheets().add(
                java.util.Objects.requireNonNull(
                        getClass().getResource("/style.css")
                ).toExternalForm()
        );



        status.getStyleClass().add("status-label");
        stage.setTitle("SmartInventory");
        stage.setScene(scene);
        stage.show();

        loadProducts();
    }


    private void updateDashboard() {
        int totalQuantity = 0;
        double totalValue = 0;

        for (Product product : allProducts) {
            totalQuantity += product.getQuantity();
            totalValue += product.getPrice() * product.getQuantity();
        }

        productsCard.setText(String.valueOf(allProducts.size()));
        stockCard.setText(String.valueOf(totalQuantity));
        valueCard.setText(String.format(
                java.util.Locale.GERMANY,
                "%,.2f €",
                totalValue
        ));

        valueCard.setText(String.format(
                java.util.Locale.GERMANY,
                "%,.2f €",
                totalValue
        ));

    }

    // Produkte aus PostgreSQL laden

    private void loadProducts() {
        try {
            allProducts.setAll(repository.getAllProducts());
            updateDashboard();


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
