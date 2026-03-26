package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.StockTableRecord;

import java.util.List;

import static schema.tables.StockTable.STOCK_TABLE;

public class TestJooq {
    public static void main(String[] args) {
        DSLContext ctx = JooqConnection.getDSLContext();

        createItem(ctx, "Paracetomal", 50f, 300);
        createItem(ctx, "Aspirin", 40f, 500);

        List<Stock> items = getAllItems(ctx);
        for (Stock item : items) {
            System.out.println(
                    item.getId() + " | " +
                            item.getName() + " | " +
                            item.getbulkCost() + " | " +
                            item.getQuantity()
            );
        }

        updateQuantity(ctx,"Paracetamol", 350);
        deleteItem(ctx, "Aspirin");
    }
    public static int createItem(DSLContext ctx, String name, float bulkCost, int quantity) {
        return ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, name)
                .set(STOCK_TABLE.BULK_COST, bulkCost)
                .set(STOCK_TABLE.QUANTITY, quantity)
                .execute();
    }

    public static List<Stock> getAllItems(DSLContext ctx) {
        Result<StockTableRecord> rows = ctx.selectFrom(STOCK_TABLE).fetch();

        return rows.map(row -> new Stock(
                row.getItemId(),
                row.getName(),
                row.getBulkCost(),
                row.getQuantity()
        ));
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
