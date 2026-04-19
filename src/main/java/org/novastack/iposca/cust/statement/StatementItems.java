package org.novastack.iposca.cust.statement;

import java.time.LocalDate;

/**
 * A java bean to hold the required statement data.
 * */
public class StatementItems {
    private String productName;
    private int quantity;
    private float price;
    private LocalDate saleDate;

    public StatementItems(String productName, int quantity, float price, LocalDate saleDate) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.saleDate = saleDate;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }
}
