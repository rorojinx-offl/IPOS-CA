package org.novastack.iposca.rpt.model;

public class StockItem {
    private String productId;
    private String productName;
    private int quantityOnHand;
    private int reorderThreshold;
    private boolean lowStock;
    private float unitCost;
    private float retailPrice;
    private float vatRate;
    private float vatAmount;
    private float totalStockValue;

    public StockItem() {}
}
