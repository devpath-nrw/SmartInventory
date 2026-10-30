package org.example;

import java.util.Scanner;
import java.sql.SQLException;
public class Main {

    public static void main(String[] args) throws SQLException {

        Scanner scanner = new Scanner(System.in);
        InventoryManager inventory = new InventoryManager();

        boolean running = true;

        while (running) {

            System.out.println("\n===== SMART INVENTORY =====");
            System.out.println("1. Produkt hinzufügen");
            System.out.println("2. Alle Produkte anzeigen");
            System.out.println("3. Produkt suchen");
            System.out.println("4. Produkt löschen");
            System.out.println("5. Produkt bearbeiten");
            System.out.println("0. Programm beenden");
            System.out.print("Deine Auswahl: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    try {
                        System.out.print("Produkt-ID: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        if (inventory.findProduct(id) != null) {
                            System.out.println("Diese ID existiert bereits!");
                            break;
                        }

                        System.out.print("Produktname: ");
                        String name = scanner.nextLine();

                        System.out.print("Preis: ");
                        double price = Double.parseDouble(scanner.nextLine());

                        System.out.print("Menge: ");
                        int quantity = Integer.parseInt(scanner.nextLine());

                        if (name.isBlank() || price < 0 || quantity < 0) {
                            System.out.println("Ungültige Produktdaten!");
                            break;
                        }

                        inventory.addProduct(
                                new Product(id, name, price, quantity)
                        );

                    } catch (NumberFormatException e) {
                        System.out.println("Bitte gültige Zahlen eingeben!");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ungültige Produktdaten: " + e.getMessage());
                    }
                    break;

                case "2":
                    inventory.showAllProducts();
                    break;

                case "3":
                    try {
                        System.out.print("Gesuchte Produkt-ID: ");
                        int searchId = Integer.parseInt(scanner.nextLine());

                        Product found = inventory.findProduct(searchId);

                        if (found != null) {
                            found.displayInfo();
                        } else {
                            System.out.println("Produkt nicht gefunden!");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Ungültige ID!");
                    }
                    break;

                case "4":
                    try {
                        System.out.print("Produkt-ID zum Löschen: ");
                        int deleteId = Integer.parseInt(scanner.nextLine());

                        inventory.removeProduct(deleteId);

                    } catch (NumberFormatException e) {
                        System.out.println("Ungültige ID!");
                    }
                    break;


                case "5":
                    try {
                        System.out.print("Produkt-ID: ");
                        int updateId = Integer.parseInt(scanner.nextLine());

                        System.out.print("Neuer Preis: ");
                        double newPrice = Double.parseDouble(scanner.nextLine());

                        System.out.print("Neue Menge: ");
                        int newQuantity = Integer.parseInt(scanner.nextLine());

                        inventory.updateProduct(updateId, newPrice, newQuantity);

                    } catch (NumberFormatException e) {
                        System.out.println("Bitte gültige Zahlen eingeben!");
                    }
                    break;


                case "0":
                    running = false;
                    System.out.println("Programm beendet.");
                    break;

                default:
                    System.out.println("Ungültige Auswahl!");
            }
        }

        scanner.close();
    }
}