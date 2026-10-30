package org.example;

public class Product {
    private int id;
    private String name;
    private double price;
    private int quantity;

    public Product(int id, String name, double price, int quantity) {

        if (id <= 0) {
            throw new IllegalArgumentException("ID muss positiv sein.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein.");
        }

        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Ungültiger Preis.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Menge darf nicht negativ sein.");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }

    // Produktinformationen anzeigen
    public void displayInfo() {
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Preis: " + price + " EUR");
        System.out.println("Bestand: " + quantity);
        System.out.println("--------------------");
    }
}



