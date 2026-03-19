package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import static schema.tables.StockTable.STOCK_TABLE;

public class TestJooq {
    static void main() {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, "Paracetamol")
                .set(STOCK_TABLE.BULK_COST, 50f)
                .set(STOCK_TABLE.QUANTITY, 300)
                .execute();
        ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, "Aspirin")
                .set(STOCK_TABLE.BULK_COST, 50f)
                .set(STOCK_TABLE.QUANTITY, 500)
                .execute();
        ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, "Analgin")
                .set(STOCK_TABLE.BULK_COST, 50f)
                .set(STOCK_TABLE.QUANTITY, 200)
                .execute();
    }
}
