package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.StockTableRecord;

import java.util.ArrayList;
import java.util.List;

import static schema.tables.StockTable.STOCK_TABLE;

public class TestJooq {
    public static void main(String[] args) {
        DSLContext ctx = JooqConnection.getDSLContext();

        createItem(ctx, "Paracetamol", 50f, 300);
        createItem(ctx, "Aspirin", 40f, 500);

        System.out.println(getAllStock());

        updateQuantity(ctx,1, 350);
        deleteItem(ctx, 2);

    }
    public static int createItem(DSLContext ctx, String name, float bulkCost, int quantity) {
        return ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, name)
                .set(STOCK_TABLE.BULK_COST, bulkCost)
                .set(STOCK_TABLE.QUANTITY, quantity)
                .execute();
    }

    public static ArrayList<Stock> getAllStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(STOCK_TABLE).where(STOCK_TABLE.QUANTITY.gt(0)).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK_TABLE.ITEM_ID.getValue(record),
                    STOCK_TABLE.NAME.getValue(record),
                    STOCK_TABLE.BULK_COST.getValue(record),
                    STOCK_TABLE.QUANTITY.getValue(record)
            ));
        });
        return inventory;
    }

    public static int updateQuantity(DSLContext ctx, int itemId, int newQuantity) {
        return ctx.update(STOCK_TABLE)
                .set(STOCK_TABLE.QUANTITY, newQuantity)
                .where(STOCK_TABLE.ITEM_ID.eq(itemId))
                .execute();
    }

    public static int deleteItem(DSLContext ctx, int itemId) {
        return ctx.deleteFrom(STOCK_TABLE)
                .where(STOCK_TABLE.ITEM_ID.eq(itemId))
                .execute();
    }
}
