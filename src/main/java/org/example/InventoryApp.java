
package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class InventoryApp extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Smart Inventory Management");

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(title);

        Scene scene = new Scene(layout, 900, 600);

        stage.setTitle("SmartInventory");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

