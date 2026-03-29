package http;

import db.JooqConnection;
import org.jooq.DSLContext;
import static schema.tables.Stock.STOCK;

public class Stock {
    private int id;
    private String name;
    private float cost;
    private int quantity;
    public record MutateStockRequest(int id, int quantity) {}

    public Stock(int id, String name, float cost, int quantity) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.quantity = quantity;
    }

    public static int mutateStock(int id, int quantity) {
        DSLContext ctx = JooqConnection.getDSLContext();
        int rowsUpdated = ctx.update(STOCK)
                .set(STOCK.QUANTITY, STOCK.QUANTITY.minus(quantity))
                .where(STOCK.ID.eq(id))
                .and(STOCK.QUANTITY.ge(quantity))
                .execute();

        if (rowsUpdated == 1) {
            return 200;
        } else {
            return 404;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getCost() {
        return cost;
    }

    public int getQuantity() {
        return quantity;
    }
}
