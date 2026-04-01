package org.novastack.iposca.rpt.model;

public class StockItem {
    private int id;
    private String name;
    private int quantity;
    private int reorderThreshold;
    private boolean lowStock;
    private float bulkCost;
    private float retailPrice;
    private float vatRate;
    private float vatAmount;
    private float totalStockValue;

    public StockItem() {}

    public StockItem(int id, String name, float bulkCost, int quantity, int reorderThreshold) {
        this.id = id;
        this.name = name;
        this.bulkCost = bulkCost;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
        this.lowStock = quantity <= reorderThreshold;
        this.vatRate = 0;
        this.vatAmount = 0;
        this.totalStockValue = bulkCost * quantity;
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

    public float getBulkCost() {
        return bulkCost;
    }

    public void setBulkCost(float bulkCost) {
        this.bulkCost = bulkCost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
        this.lowStock = quantity <= reorderThreshold;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }

    public float getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(float retailPrice) {
        this.retailPrice = retailPrice;
    }

    public float getVatRate() {
        return vatRate;
    }

    public void setVatRate(float vatRate) {
        this.vatRate = vatRate;
    }

    public float getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(float vatAmount) {
        this.vatAmount = vatAmount;
    }

    public float getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(float totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

}
