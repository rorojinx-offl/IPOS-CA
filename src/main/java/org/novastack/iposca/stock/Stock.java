package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;
import static schema.tables.Stock.STOCK;

public class Stock {
    private int id;
    private String name;
    private String productType;
    private String packageType;
    private String units;
    private int unitsInAPack;
    private float bulkCost;
    private int markupRate;
    private int quantity;
    private int stockLimit;
    public record PUStockRequest(int id, int quantity) {}
    public record SAStockRequest(String name, String packageType, String units , int unitsInAPack, float bulkCost, int markupRate, int quantity, int stockLimit) {}

    public Stock(String name, String productType, String packageType, String units, int unitsInAPack, float bulkCost, int markupRate, int quantity, int stockLimit) {
        this.name = name;
        this.productType = productType;
        this.packageType = packageType;
        this.units = units;
        this.unitsInAPack = unitsInAPack;
        this.bulkCost = bulkCost;
        this.markupRate = markupRate;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }

    public Stock(String name, String packageType, String units, int unitsInAPack, float bulkCost, int markupRate, int quantity, int stockLimit) {
        this.name = name;
        this.packageType = packageType;
        this.units = units;
        this.unitsInAPack = unitsInAPack;
        this.bulkCost = bulkCost;
        this.markupRate = markupRate;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }

    public Stock(int id, String name, String productType, String packageType, String units, int unitsInAPack, float bulkCost, int markupRate, int quantity, int stockLimit) {
        this.id=id;
        this.name = name;
        this.productType = productType;
        this.packageType = packageType;
        this.units = units;
        this.unitsInAPack = unitsInAPack;
        this.bulkCost = bulkCost;
        this.markupRate = markupRate;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }

    public Stock(int id, String name, String productType, String packageType, String units, int unitsInAPack, float bulkCost, int quantity, int stockLimit) {
        this.id=id;
        this.name = name;
        this.productType = productType;
        this.packageType = packageType;
        this.units = units;
        this.unitsInAPack = unitsInAPack;
        this.bulkCost = bulkCost;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }

    public Stock(int id, String name, int markupRate) {
        this.id=id;
        this.name = name;
        this.markupRate = markupRate;
    }

    public Stock(int id, String name, float bulkCost, int quantity) {
        this.id = id;
        this.name = name;
        this.bulkCost = bulkCost;
        this.quantity = quantity;
    }
    // create item entry and populate database
    public void createItem(Stock stock) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(STOCK)
                .set(STOCK.NAME, stock.getName())
                .set(STOCK.PRODUCT_TYPE, stock.getProductType())
                .set(STOCK.PACKAGE_TYPE, stock.getPackageType())
                .set(STOCK.UNITS, stock.getUnits())
                .set(STOCK.UNITS_IN_A_PACK, stock.getUnitsInAPack())
                .set(STOCK.BULK_COST, stock.getBulkCost())
                .set(STOCK.MARKUP_RATE, stock.getMarkupRate())
                .set(STOCK.QUANTITY, stock.getQuantity())
                .set(STOCK.STOCK_LIMIT, stock.getStockLimit())
                .execute();
    }
    // link between CA and SA
    public static int upsertIPOSItem(Stock stock) {
        DSLContext ctx = JooqConnection.getDSLContext();

        int rowsAffected = ctx.insertInto(STOCK)
                .set(STOCK.NAME, stock.getName())
                .set(STOCK.PRODUCT_TYPE, StockEnums.ProductType.IPOS.name())
                .set(STOCK.PACKAGE_TYPE, stock.getPackageType())
                .set(STOCK.UNITS, stock.getUnits())
                .set(STOCK.UNITS_IN_A_PACK, stock.getUnitsInAPack())
                .set(STOCK.BULK_COST, stock.getBulkCost())
                .set(STOCK.MARKUP_RATE, stock.getMarkupRate())
                .set(STOCK.QUANTITY, stock.getQuantity())
                .set(STOCK.STOCK_LIMIT, stock.getStockLimit())
                .onConflict(STOCK.NAME)
                .doUpdate()
                .set(STOCK.QUANTITY, STOCK.QUANTITY.plus(stock.getQuantity()))
                .execute();

        if (rowsAffected == 1) {
            return 200;
        } else {
            return 404;
        }
    }

    public static ArrayList<Stock> getAllStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(STOCK).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK.ITEM_ID.getValue(record),
                    STOCK.NAME.getValue(record),
                    STOCK.PRODUCT_TYPE.getValue(record),
                    STOCK.PACKAGE_TYPE.getValue(record),
                    STOCK.UNITS.getValue(record),
                    STOCK.UNITS_IN_A_PACK.getValue(record),
                    STOCK.BULK_COST.getValue(record),
                    STOCK.MARKUP_RATE.getValue(record),
                    STOCK.QUANTITY.getValue(record),
                    STOCK.STOCK_LIMIT.getValue(record)
            ));
        });

        return inventory;
    }
    // calculate retail price, and minimizes info shown (abstraction layer)
    public static ArrayList<Stock> getAllStockForSale() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(STOCK).where(STOCK.QUANTITY.gt(0)).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK.ITEM_ID.getValue(record),
                    STOCK.NAME.getValue(record),
                    calcRetailPrice(STOCK.BULK_COST.getValue(record), STOCK.MARKUP_RATE.getValue(record)),
                    STOCK.QUANTITY.getValue(record)
            ));
        });
        return inventory;
    }

    private static float calcRetailPrice(float bulkCost, int markupRate) {
        return bulkCost * (1 + (markupRate / 100f));
    }
    // collects all stock that is below a certain thresold, refrences stock limit column in the table
    public static ArrayList<Stock> getLowStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        double percentage = 0.2;
        ctx.selectFrom(STOCK).where(STOCK.QUANTITY.le(STOCK.STOCK_LIMIT)).fetch().forEach(record -> {
                    inventory.add(new Stock(
                                    STOCK.ITEM_ID.getValue(record),
                                    STOCK.NAME.getValue(record),
                                    STOCK.PRODUCT_TYPE.getValue(record),
                                    STOCK.PACKAGE_TYPE.getValue(record),
                                    STOCK.UNITS.getValue(record),
                                    STOCK.UNITS_IN_A_PACK.getValue(record),
                                    STOCK.BULK_COST.getValue(record),
                                    STOCK.MARKUP_RATE.getValue(record),
                                    STOCK.QUANTITY.getValue(record),
                                    STOCK.STOCK_LIMIT.getValue(record)
                            )
                    );
                }
        );
        return inventory;
    }

    public void editStock(Stock stock) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(STOCK)
                .set(STOCK.NAME, stock.getName())
                .set(STOCK.PRODUCT_TYPE, stock.getProductType())
                .set(STOCK.PACKAGE_TYPE, stock.getPackageType())
                .set(STOCK.UNITS, stock.getUnits())
                .set(STOCK.UNITS_IN_A_PACK, stock.getUnitsInAPack())
                .set(STOCK.BULK_COST, stock.getBulkCost())
                .set(STOCK.QUANTITY, stock.getQuantity())
                .set(STOCK.STOCK_LIMIT, stock.getStockLimit())
                .where(STOCK.ITEM_ID.eq(stock.getId()))
                .execute();
    }

    public static int deleteItem(int itemId) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.deleteFrom(STOCK)
                .where(STOCK.ITEM_ID.eq(itemId))
                .execute();
    }
    //
    public static ArrayList<Stock> getAllMarkupRates() {
        DSLContext ctx = JooqConnection.getDSLContext();
        ArrayList<Stock> inventory = new ArrayList<>();
        ctx.selectFrom(STOCK).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK.ITEM_ID.getValue(record),
                    STOCK.NAME.getValue(record),
                    STOCK.MARKUP_RATE.getValue(record)
            ));
        });
        return inventory;
    }

    public static void changeMarkupRate(int id, int markupRate) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(STOCK)
                .set(STOCK.MARKUP_RATE, markupRate)
                .where(STOCK.ITEM_ID.eq(id))
                .execute();
    }
    // link to PU and Sales-CA package, decrments only since incrementation occurs in upsertItem
    public static int minusStock(int productID, int quantity) {
        DSLContext ctx = JooqConnection.getDSLContext();
        int rowsUpdated = ctx.update(STOCK)
                .set(STOCK.QUANTITY, STOCK.QUANTITY.minus(quantity))
                .where(STOCK.ITEM_ID.eq(productID))
                .and(STOCK.QUANTITY.ge(quantity))
                .execute();

        if (rowsUpdated == 1) {
            return 200;
        } else {
            return 404;
        }
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

    public String getProductType() {
        return productType;
    }

    public String getPackageType() {
        return packageType;
    }

    public String getUnits() {
        return units;
    }

    public int getUnitsInAPack() {
        return unitsInAPack;
    }

    public int getMarkupRate() {
        return markupRate;
    }

    public int getStockLimit() {
        return stockLimit;
    }
}
