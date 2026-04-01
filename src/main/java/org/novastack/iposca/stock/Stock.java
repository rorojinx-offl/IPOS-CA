package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static schema.tables.StockTable.STOCK_TABLE;

//creating stock objects
public class Stock {
    private int id;
    private String name;
    private String productType;
    private String packageType;
    private String units;
    private int unitsInAPack;
    private float bulkCost;
    private int quantity;
    private int stockLimit;


    public Stock(String name, String productType, String packageType, String units, int unitsInAPack, float bulkCost, int quantity, int stockLimit) {
        this.name = name;
        this.productType = productType;
        this.packageType = packageType;
        this.units = units;
        this.unitsInAPack = unitsInAPack;
        this.bulkCost = bulkCost;
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


    public void createItem(Stock stock) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(STOCK_TABLE)
                .set(STOCK_TABLE.NAME, stock.getName())
                .set(STOCK_TABLE.PRODUCT_TYPE, stock.getProductType())
                .set(STOCK_TABLE.PACKAGE_TYPE, stock.getPackageType())
                .set(STOCK_TABLE.UNITS, stock.getUnits())
                .set(STOCK_TABLE.UNITS_IN_A_PACK, stock.getUnitsInAPack())
                .set(STOCK_TABLE.BULK_COST, stock.getBulkCost())
                .set(STOCK_TABLE.QUANTITY, stock.getQuantity())
                .set(STOCK_TABLE.STOCK_LIMIT, stock.getStockLimit())
                .execute();
    }

    public static ArrayList<Stock> getAllStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(STOCK_TABLE).where(STOCK_TABLE.QUANTITY.gt(0)).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK_TABLE.ITEM_ID.getValue(record),
                    STOCK_TABLE.NAME.getValue(record),
                    STOCK_TABLE.PRODUCT_TYPE.getValue(record),
                    STOCK_TABLE.PACKAGE_TYPE.getValue(record),
                    STOCK_TABLE.UNITS.getValue(record),
                    STOCK_TABLE.UNITS_IN_A_PACK.getValue(record),
                    STOCK_TABLE.BULK_COST.getValue(record),
                    STOCK_TABLE.QUANTITY.getValue(record),
                    STOCK_TABLE.STOCK_LIMIT.getValue(record)
            ));
        });
        return inventory;
    }

    public static ArrayList<Stock> getLowStock() {
        ArrayList<Stock> inventory = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        double percentage = 0.2;
        ctx.selectFrom(STOCK_TABLE).where(STOCK_TABLE.QUANTITY.le(DSL.round(STOCK_TABLE.STOCK_LIMIT.mul(percentage)))).fetch().forEach(record -> {
            inventory.add(new Stock(
                    STOCK_TABLE.ITEM_ID.getValue(record),
                    STOCK_TABLE.NAME.getValue(record),
                    STOCK_TABLE.PRODUCT_TYPE.getValue(record),
                    STOCK_TABLE.PACKAGE_TYPE.getValue(record),
                    STOCK_TABLE.UNITS.getValue(record),
                    STOCK_TABLE.UNITS_IN_A_PACK.getValue(record),
                    STOCK_TABLE.BULK_COST.getValue(record),
                    STOCK_TABLE.QUANTITY.getValue(record),
                    STOCK_TABLE.STOCK_LIMIT.getValue(record)
                    )
            );
                }
        );
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

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int getUnitsInAPack() {
        return unitsInAPack;
    }

    public void setUnitsInAPack(int unitsInAPack) {
        this.unitsInAPack = unitsInAPack;
    }

    public float getBulkCost() {
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

    public int getStockLimit() {
        return stockLimit;
    }

}
