package org.example;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * SmartInventory: JavaFX-Oberfläche für die vorhandene PostgreSQL-Anwendung.
 * Produktdaten, Suche, CRUD und Kennzahlen verwenden weiterhin ProductRepository.
 */
public class InventoryApp extends Application {

    private final ProductRepository repository = new ProductRepository();
    private final TableView<Product> table = new TableView<>();
    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final FilteredList<Product> filteredProducts =
            new FilteredList<>(allProducts, product -> true);

    private final TextField searchField = new TextField();
    private final Label productsValue = new Label("0");
    private final Label stockValue = new Label("0");
    private final Label inventoryValue = new Label("0,00 €");
    private final Label resultsLabel = new Label("0 Einträge");
    private final Label status = new Label("Bereit");
    private final Label pageTitle = new Label("Dashboard");
    private final Label pageSubtitle = new Label("Alle wichtigen Lagerdaten auf einen Blick.");

    private HBox statistics;
    private Button dashboardNav;
    private Button productsNav;
    private String stylesheet;

    @Override
    public void start(Stage stage) {
        stylesheet = Objects.requireNonNull(
                getClass().getResource("/style.css"), "style.css wurde nicht gefunden"
        ).toExternalForm();

        configureTable();
        configureSearch();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("SmartInventory – Lagerverwaltung");
        stage.setMinWidth(1050);
        stage.setMinHeight(690);
        stage.setScene(scene);
        stage.show();

        showPage(true);
        loadProducts();
    }

    // Sidebar: Beide Navigationspunkte haben eine echte Funktion.
    private VBox createSidebar() {
        Label logo = new Label("▦");
        logo.getStyleClass().add("brand-symbol");

        Label brand = new Label("SmartInventory");
        brand.getStyleClass().add("brand-title");
        Label brandDescription = new Label("INVENTORY MANAGER");
        brandDescription.getStyleClass().add("brand-description");

        VBox brandText = new VBox(4, brand, brandDescription);
        HBox branding = new HBox(12, logo, brandText);
        branding.setAlignment(Pos.CENTER_LEFT);
        branding.getStyleClass().add("branding");

        Label section = new Label("ARBEITSBEREICH");
        section.getStyleClass().add("sidebar-section-label");

        dashboardNav = createNavigationButton("▦", "Dashboard");
        productsNav = createNavigationButton("▤", "Produkte");
        dashboardNav.setOnAction(event -> showPage(true));
        productsNav.setOnAction(event -> showPage(false));

        VBox navigation = new VBox(8, section, dashboardNav, productsNav);
        navigation.getStyleClass().add("sidebar-navigation");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footerTitle = new Label("DESKTOP APPLICATION");
        footerTitle.getStyleClass().add("sidebar-footer-title");
        Label footerText = new Label("JavaFX 21  ·  PostgreSQL");
        footerText.getStyleClass().add("sidebar-footer-text");
        VBox footer = new VBox(7, footerTitle, footerText);
        footer.getStyleClass().add("sidebar-footer");

        VBox sidebar = new VBox(32, branding, navigation, spacer, footer);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(236);
        return sidebar;
    }

    private Button createNavigationButton(String symbol, String text) {
        Label icon = new Label(symbol);
        icon.getStyleClass().add("nav-icon");
        Button button = new Button(text, icon);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private VBox createMainContent() {
        pageTitle.getStyleClass().add("page-title");
        pageSubtitle.getStyleClass().add("page-subtitle");
        VBox heading = new VBox(6, pageTitle, pageSubtitle);

        Label headerTag = new Label("LAGERÜBERSICHT");
        headerTag.getStyleClass().add("header-tag");
        HBox header = new HBox(16, heading, stretch(), headerTag);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("page-header");

        statistics = new HBox(16,
                createStatCard("Gesamtprodukte", "▦", productsValue, "blue"),
                createStatCard("Artikel auf Lager", "▤", stockValue, "teal"),
                createStatCard("Gesamter Lagerwert", "€", inventoryValue, "purple")
        );
        statistics.getStyleClass().add("statistics-row");
        for (javafx.scene.Node item : statistics.getChildren()) {
            HBox.setHgrow(item, Priority.ALWAYS);
        }

        VBox productsPanel = createProductsPanel();
        VBox.setVgrow(productsPanel, Priority.ALWAYS);

        Label statusDot = new Label("●");
        statusDot.getStyleClass().add("status-dot");
        status.getStyleClass().add("status-text");
        HBox statusBar = new HBox(9, statusDot, status);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar");

        VBox content = new VBox(22, header, statistics, productsPanel, statusBar);
        content.getStyleClass().add("main-content");
        return content;
    }

    private VBox createStatCard(String description, String symbol, Label value, String accent) {
        Label icon = new Label(symbol);
        icon.getStyleClass().addAll("stat-icon", "stat-icon-" + accent);

        Label title = new Label(description);
        title.getStyleClass().add("stat-caption");
        HBox top = new HBox(10, icon, title);
        top.setAlignment(Pos.CENTER_LEFT);

        value.getStyleClass().add("stat-value");
        VBox card = new VBox(15, top, value);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox createProductsPanel() {
        Label title = new Label("Produktverwaltung");
        title.getStyleClass().add("panel-title");
        Label subtitle = new Label("Artikel suchen, bearbeiten und verwalten");
        subtitle.getStyleClass().add("panel-subtitle");
        VBox panelHeading = new VBox(4, title, subtitle);

        Button addButton = createActionButton("＋", "Produkt hinzufügen", "action-primary");
        addButton.setOnAction(event -> showProductForm(null));

        HBox upperToolbar = new HBox(16, panelHeading, stretch(), addButton);
        upperToolbar.setAlignment(Pos.CENTER_LEFT);

        Label searchIcon = new Label("⌕");
        searchIcon.getStyleClass().add("search-icon");
        HBox searchContainer = new HBox(8, searchIcon, searchField);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getStyleClass().add("search-container");
        searchContainer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox.setHgrow(searchContainer, Priority.ALWAYS);

        Button editButton = createActionButton("✎", "Bearbeiten", "action-secondary");
        editButton.setOnAction(event -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Bitte zuerst ein Produkt aus der Tabelle auswählen.");
                return;
            }
            showProductForm(selected);
        });

        Button deleteButton = createActionButton("×", "Löschen", "action-danger");
        deleteButton.setOnAction(event -> deleteSelectedProduct());

        Button refreshButton = createActionButton("↻", "Aktualisieren", "action-refresh");
        refreshButton.setOnAction(event -> loadProducts());

        HBox lowerToolbar = new HBox(10, searchContainer, editButton, deleteButton, refreshButton);
        lowerToolbar.setAlignment(Pos.CENTER_LEFT);
        lowerToolbar.getStyleClass().add("table-toolbar");

        Label listTitle = new Label("Alle Produkte");
        listTitle.getStyleClass().add("table-section-title");
        resultsLabel.getStyleClass().add("results-label");
        HBox listHeader = new HBox(12, listTitle, stretch(), resultsLabel);
        listHeader.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(19, upperToolbar, lowerToolbar, listHeader, table);
        panel.getStyleClass().add("products-panel");
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private Button createActionButton(String iconText, String title, String styleClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("action-icon");
        Button button = new Button(title, icon);
        button.getStyleClass().addAll("action-button", styleClass);
        return button;
    }

    private Region stretch() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void configureTable() {
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Product, String> nameColumn = new TableColumn<>("PRODUKTNAME");
        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getName()));

        TableColumn<Product, Double> priceColumn = new TableColumn<>("PREIS");
        priceColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : formatEuro(price));
            }
        });
        priceColumn.getStyleClass().add("number-column");

        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("BESTAND");
        quantityColumn.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        quantityColumn.getStyleClass().add("number-column");

        table.getColumns().addAll(idColumn, nameColumn, priceColumn, quantityColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("product-table");
        table.setPlaceholder(new Label("Keine Produkte gefunden."));

        SortedList<Product> sorted = new SortedList<>(filteredProducts);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void configureSearch() {
        searchField.setPromptText("Produktname oder ID suchen ...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue.toLowerCase(Locale.ROOT).trim();
            filteredProducts.setPredicate(product ->
                    product.getName().toLowerCase(Locale.ROOT).contains(query)
                            || String.valueOf(product.getId()).contains(query));
            updateResultCount();
        });
    }

    // Dashboard-Ansicht: Kennzahlen sichtbar; Produkte-Ansicht: Fokus auf Tabelle.
    private void showPage(boolean dashboard) {
        statistics.setVisible(dashboard);
        statistics.setManaged(dashboard);
        pageTitle.setText(dashboard ? "Dashboard" : "Produkte");
        pageSubtitle.setText(dashboard
                ? "Alle wichtigen Lagerdaten auf einen Blick."
                : "Deinen Produktbestand schnell und einfach verwalten.");

        dashboardNav.getStyleClass().remove("nav-active");
        productsNav.getStyleClass().remove("nav-active");
        (dashboard ? dashboardNav : productsNav).getStyleClass().add("nav-active");

        if (!dashboard) {
            searchField.requestFocus();
        }
    }

    private void updateResultCount() {
        int count = filteredProducts.size();
        resultsLabel.setText(count + (count == 1 ? " Eintrag" : " Einträge"));
    }

    private void updateDashboard() {
        long totalQuantity = 0;
        double totalValue = 0;
        for (Product product : allProducts) {
            totalQuantity += product.getQuantity();
            totalValue += product.getPrice() * product.getQuantity();
        }
        productsValue.setText(String.valueOf(allProducts.size()));
        stockValue.setText(String.valueOf(totalQuantity));
        inventoryValue.setText(formatEuro(totalValue));
    }

    private String formatEuro(double value) {
        return String.format(Locale.GERMANY, "%,.2f €", value);
    }

    private void loadProducts() {
        try {
            allProducts.setAll(repository.getAllProducts());
            updateDashboard();
            updateResultCount();
            setStatus("Produkte erfolgreich geladen.", false);
        } catch (SQLException exception) {
            showError("Datenbankfehler: " + exception.getMessage());
        }
    }

    private void showProductForm(Product existing) {
        boolean editing = existing != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(table.getScene().getWindow());
        dialog.setTitle(editing ? "Produkt bearbeiten" : "Produkt hinzufügen");

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();

        idField.setPromptText("z. B. 101");
        nameField.setPromptText("z. B. DELL Laptop");
        priceField.setPromptText("z. B. 799,99");
        quantityField.setPromptText("z. B. 20");

        if (editing) {
            idField.setText(String.valueOf(existing.getId()));
            idField.setDisable(true);
            nameField.setText(existing.getName());
            priceField.setText(String.valueOf(existing.getPrice()).replace('.', ','));
            quantityField.setText(String.valueOf(existing.getQuantity()));
        }

        GridPane fields = new GridPane();
        fields.setHgap(16);
        fields.setVgap(17);
        fields.add(createFormField("Produkt-ID", idField), 0, 0);
        fields.add(createFormField("Produktname", nameField), 1, 0);
        fields.add(createFormField("Preis (€)", priceField), 0, 1);
        fields.add(createFormField("Lagerbestand", quantityField), 1, 1);

        Label heading = new Label(editing ? "Produkt bearbeiten" : "Neues Produkt");
        heading.getStyleClass().add("dialog-heading");
        Label description = new Label(editing
                ? "Passe die Produktinformationen an. Die ID bleibt unverändert."
                : "Erfasse die Angaben für einen neuen Lagerartikel.");
        description.getStyleClass().add("dialog-description");

        Label formError = new Label();
        formError.getStyleClass().add("form-error");
        formError.setWrapText(true);
        formError.setManaged(false);
        formError.setVisible(false);

        Label hint = new Label("Alle Felder sind erforderlich. Preis und Bestand dürfen nicht negativ sein.");
        hint.getStyleClass().add("form-hint");
        hint.setWrapText(true);

        VBox form = new VBox(16, heading, description, fields, hint, formError);
        form.getStyleClass().add("dialog-form");
        dialog.getDialogPane().setContent(form);

        ButtonType saveType = new ButtonType(
                editing ? "Änderungen speichern" : "Produkt hinzufügen",
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, saveType);
        applyDialogStyle(dialog);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.getStyleClass().add("dialog-primary-button");
        dialog.getDialogPane().lookupButton(cancelType)
                .getStyleClass().add("dialog-secondary-button");

        // Ungültige Eingaben lassen den Dialog offen, damit Daten nicht verloren gehen.
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim().replace(",", "."));
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (!Double.isFinite(price)) {
                    throw new IllegalArgumentException("Der Preis muss eine gültige Zahl sein.");
                }

                Product product = new Product(id, name, price, quantity);
                if (editing) {
                    if (!repository.updateProduct(product)) {
                        throw new IllegalArgumentException("Produkt wurde nicht gefunden.");
                    }
                } else {
                    repository.addProduct(product);
                }

                loadProducts();
                setStatus(editing ? "Produkt erfolgreich bearbeitet."
                        : "Produkt erfolgreich hinzugefügt.", false);
            } catch (NumberFormatException exception) {
                showFormError(formError, "Bitte gültige Zahlen für ID, Preis und Bestand eingeben.");
                event.consume();
            } catch (IllegalArgumentException exception) {
                showFormError(formError, "Ungültige Produktdaten: " + exception.getMessage());
                event.consume();
            } catch (SQLException exception) {
                showFormError(formError, "Datenbankfehler: " + exception.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private VBox createFormField(String title, TextField input) {
        Label label = new Label(title);
        label.getStyleClass().add("field-label");
        input.getStyleClass().add("form-input");
        input.setPrefWidth(240);
        VBox field = new VBox(7, label, input);
        GridPane.setHgrow(field, Priority.ALWAYS);
        return field;
    }

    private void showFormError(Label label, String message) {
        label.setText(message);
        label.setManaged(true);
        label.setVisible(true);
    }

    private void deleteSelectedProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Bitte zuerst ein Produkt aus der Tabelle auswählen.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(table.getScene().getWindow());
        confirm.setTitle("Produkt löschen");
        confirm.setHeaderText("Produkt wirklich löschen?");
        confirm.setContentText("„" + selected.getName()
                + "“ (ID " + selected.getId() + ") wird dauerhaft entfernt.");

        ButtonType cancelType = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteType = new ButtonType("Produkt löschen", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(cancelType, deleteType);
        applyDialogStyle(confirm);
        confirm.getDialogPane().lookupButton(deleteType)
                .getStyleClass().add("dialog-danger-button");
        confirm.getDialogPane().lookupButton(cancelType)
                .getStyleClass().add("dialog-secondary-button");

        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != deleteType) {
            return;
        }

        try {
            if (repository.deleteProduct(selected.getId())) {
                loadProducts();
                setStatus("Produkt erfolgreich gelöscht.", false);
            } else {
                showWarning("Produkt wurde nicht gefunden.");
            }
        } catch (SQLException exception) {
            showError("Datenbankfehler: " + exception.getMessage());
        }
    }

    private void applyDialogStyle(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(stylesheet);
        dialog.getDialogPane().getStyleClass().add("app-dialog");
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(table.getScene().getWindow());
        alert.setTitle("Hinweis");
        alert.setHeaderText("Aktion nicht möglich");
        alert.setContentText(message);
        applyDialogStyle(alert);
        alert.showAndWait();
    }

    private void showError(String message) {
        setStatus(message, true);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(table.getScene().getWindow());
        alert.setTitle("Fehler");
        alert.setHeaderText("Ein Fehler ist aufgetreten");
        alert.setContentText(message);
        applyDialogStyle(alert);
        alert.showAndWait();
    }

    private void setStatus(String message, boolean error) {
        status.setText(message);
        status.getStyleClass().removeAll("status-error", "status-success");
        status.getStyleClass().add(error ? "status-error" : "status-success");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
