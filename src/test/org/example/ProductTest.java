package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    void testValidProduct() {
        Product product = new Product(101, "DELL", 899.99, 10);

        assertEquals(101, product.getId());
        assertEquals("DELL", product.getName());
        assertEquals(899.99, product.getPrice());
        assertEquals(10, product.getQuantity());
    }

    @Test
    void testNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product(102, "Laptop", -500, 10);
        });
    }

    @Test
    void testNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product(103, "Mouse", 25.0, -5);
        });
    }

    @Test
    void testEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product(104, "", 100.0, 5);
        });
    }

    @Test
    void testNegativeId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product(-1, "Monitor", 200.0, 3);
        });
    }
}
