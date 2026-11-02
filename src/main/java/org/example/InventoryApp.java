
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

public class InventoryApp extends Application {

    private final ProductRepository repository =
            new ProductRepository();

    private final TableView<Product> table =
            new TableView<>();

    private final Label status = new Label("Bereit");

    @Override
    public void start(Stage stage) {

        Label title =
                new Label("Smart Inventory Management");

        title.setStyle(
                "-fx-font-size: 26px; -fx-font-weight: bold;"
        );

        TableColumn<Product, Integer> idColumn =
                new TableColumn<>("ID");

        idColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getId()
                ));

        TableColumn<Product, String> nameColumn =
                new TableColumn<>("Produktname");

        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getName()
                ));

        TableColumn<Product, Double> priceColumn =
                new TableColumn<>("Preis (€)");

        priceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getPrice()
                ));

        TableColumn<Product, Integer> quantityColumn =
                new TableColumn<>("Bestand");

        quantityColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getQuantity()
                ));


        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(priceColumn);
        table.getColumns().add(quantityColumn);



        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );


        Button refreshButton =
                new Button("Aktualisieren");

        refreshButton.setOnAction(event -> loadProducts());

        VBox layout = new VBox(
                20, title, refreshButton, table, status
        );

        layout.setPadding(new Insets(25));
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 950, 600);

        stage.setTitle("SmartInventory");
        stage.setScene(scene);
        stage.show();

        loadProducts();
    }

    private void loadProducts() {
        try {
            table.setItems(
                    FXCollections.observableArrayList(
                            repository.getAllProducts()
                    )
            );

            status.setText("Produkte erfolgreich geladen.");

        } catch (SQLException e) {
            status.setText("Datenbankfehler: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
