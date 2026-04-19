package org.novastack.iposca.sales;

/**
 * Bean class that represents an item in an invoice
 * */
public class InvoiceItems {
    private String productName;
    private int quantity;
    private float price;
    private float subtotal;

    public InvoiceItems(String productName, int quantity, float price, float subtotal) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
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

    public float getSubtotal() {
        return subtotal;
    }
}
