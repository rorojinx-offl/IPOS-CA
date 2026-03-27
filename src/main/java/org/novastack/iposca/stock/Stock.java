package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;
import static schema.tables.Stock.STOCK;

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

    public static ArrayList<Stock> getAllStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(STOCK).where(STOCK.QUANTITY.gt(0)).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK.ITEM_ID.getValue(record),
                    STOCK.NAME.getValue(record),
                    STOCK.BULK_COST.getValue(record),
                    STOCK.QUANTITY.getValue(record)
            ));
        });
        return inventory;
    }

    public static String getProductName(int productID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(STOCK).where(STOCK.ITEM_ID.eq(productID)).fetchOne().getValue(STOCK.NAME);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getBulkCost() {
        return bulkCost;
    }

    public int getQuantity() {
        return quantity;
    }
}
