package org.novastack.iposca.stock;
//creating stock objects
public class Stock {
    private int id;
    private String name;
    private float bulkCost;
    private int quantity;

    public Stock(int id, String name, float bulkCost, int quantity) {
        this.id = id;
        this.name = name;
        this.bulkCost = bulkCost;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getbulkCost() {
        return bulkCost;
    }

    public void setBulkCost(float price) {
        this.bulkCost = bulkCost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
