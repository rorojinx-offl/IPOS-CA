package org.novastack.iposca.stock.report;

public class LowStockBean {
    private int itemID;
    private String name;
    private int availableQuantity;
    private int stockLimit;
    private int minOrderQty;

    public LowStockBean(int itemID, String name, int availableQuantity, int stockLimit, int minOrderQty) {
        this.itemID = itemID;
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.stockLimit = stockLimit;
        this.minOrderQty = minOrderQty;
    }

    public int getItemID() {
        return itemID;
    }

    public String getName() {
        return name;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public int getMinOrderQty() {
        return minOrderQty;
    }
}
