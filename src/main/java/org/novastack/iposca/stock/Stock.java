package org.novastack.iposca.stock;

public class Stock {
    private int id;
    private String name;
    private float price;
    private int quantity;

    public Stock(int id, String name, float price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
