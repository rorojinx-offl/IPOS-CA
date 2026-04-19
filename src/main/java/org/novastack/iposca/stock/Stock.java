package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;
import static schema.tables.Stock.STOCK;

/**
 * A comprehensive class that manages the storage, retrieval and management of stock information.
 * */
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
    /**
     * This record contains the data required to make a PU request to take stock from CA.
     * @param id The ID of the stock item.
     * @param quantity The quantity of stock to take.
     * */
    public record PUStockRequest(int id, int quantity) {}
    /**
     * This record contains the data required to make a SA request to add/update stock in CA.
     * @param name The name of the stock item.
     * @param packageType The type of packaging for the stock item.
     * @param units The units of the stock item.
     * @param unitsInAPack The number of units in a single pack.
     * @param bulkCost The bulk cost of the stock item.
     * @param markupRate The markup rate of the stock item.
     * @param quantity The quantity of stock to add/update.
     * @param stockLimit The low stock level that triggers a warning.
     * */
    public record SAStockRequest(String name, String packageType, String units , int unitsInAPack, float bulkCost, int markupRate, int quantity, int stockLimit) {}

    /**
     * Constructor for the Stock class that is used to create a new (non-IPOS) stock item.
     * @param name The name of the stock item.
     * @param productType The type of product (IPOS or non-IPOS, but usually always non-IPOS).
     * @param packageType The type of packaging for the stock item.
     * @param units The units of the stock item.
     * @param unitsInAPack The number of units in a single pack.
     * @param bulkCost The bulk cost of the stock item.
     * @param markupRate The markup rate of the stock item.
     * @param quantity The initial quantity of stock to add.
     * @param stockLimit The low stock level that triggers a warning.
     * */
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

    /**
     * Constructor for the Stock class that is used to add/update an IPOS stock item.
     * @param name The name of the stock item.
     * @param packageType The type of packaging for the stock item.
     * @param units The units of the stock item.
     * @param unitsInAPack The number of units in a single pack.
     * @param bulkCost The bulk cost of the stock item.
     * @param markupRate The markup rate of the stock item.
     * @param quantity The initial/additional quantity of stock to add.
     * @param stockLimit The low stock level that triggers a warning.
     * */
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

    /**
     * Constructor for the Stock class that is used to fetch all stock items (comprehensive) or low-stock items.
     * @param id The ID of the stock item.
     * @param name The name of the stock item.
     * @param productType The type of product (IPOS or non-IPOS, but usually always non-IPOS).
     * @param packageType The type of packaging for the stock item.
     * @param units The units of the stock item.
     * @param unitsInAPack The number of units in a single pack.
     * @param bulkCost The bulk cost of the stock item.
     * @param markupRate The markup rate of the stock item.
     * @param quantity The initial quantity of stock to add.
     * @param stockLimit The low stock level that triggers a warning.
     * */
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

    /**
     * Constructor for the Stock class that is used to edit a (non-IPOS) stock item.
     * @param id The ID of the stock item.
     * @param name The name of the stock item.
     * @param productType The type of product (IPOS or non-IPOS, but usually always non-IPOS).
     * @param packageType The type of packaging for the stock item.
     * @param units The units of the stock item.
     * @param unitsInAPack The number of units in a single pack.
     * @param bulkCost The bulk cost of the stock item.
     * @param quantity The initial quantity of stock to add.
     * @param stockLimit The low stock level that triggers a warning.
     * */
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

    /**
     * Constructor for the Stock class that is used to fetch markup rates.
     * @param id The ID of the stock item.
     * @param name The name of the stock item.
     * @param markupRate The markup rate of the stock item.
     * */
    public Stock(int id, String name, int markupRate) {
        this.id=id;
        this.name = name;
        this.markupRate = markupRate;
    }

    /**
     * Constructor for the Stock class that is used to fetch all products to display for sale.
     * @param id The ID of the stock item.
     * @param name The name of the stock item.
     * @param bulkCost The bulk cost of the stock item.
     * @param quantity The initial quantity of stock to add.
     * */
    public Stock(int id, String name, float bulkCost, int quantity) {
        this.id = id;
        this.name = name;
        this.bulkCost = bulkCost;
        this.quantity = quantity;
    }

    /**
     * Adds a new non-IPOS stock item to the database.
     * @param stock The stock item to be added.
     * */
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

    /**
     * Adds or updates an IPOS stock item in the database. It is invoked by {@link org.novastack.iposca.http.Server}
     * after a SA request following successful stock order.
     * @param stock The stock item to be added or updated.
     * @return HTTP status code in relation to rows affected. 200 if the item was added successfully, 404 if the item already exists.
     * */
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

    /**
     * Fetches all stock items from the database and is used for displaying on the debt management screen.
     * @return An {@link ArrayList} of {@link Stock} objects representing all stock items.
     * */
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

    /**
     * Fetches all the stock in a format that can be presented to the customer for sale.
     * @return An {@link ArrayList} of {@link Stock} objects representing all stock items.
     * */
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

    /**
     * Collects all stock that is below a certain threshold, references stock limit column in the table.
     * @return An {@link ArrayList} of {@link Stock} objects representing low stock items.
     * */
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

    /**
     * Updates a non-IPOS product in the database.
     * @param stock The stock item to be updated.
     * */
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

    /**
     * Deletes a non-IPOS product from the database.
     * @param itemId The ID of the stock item to be deleted.
     * */
    public static void deleteItem(int itemId) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(STOCK)
                .where(STOCK.ITEM_ID.eq(itemId))
                .execute();
    }

    /**
     * Fetches all the markup rates from the database.
     * @return An {@link ArrayList} of {@link Stock} objects representing all stock items (markup rates only).
     * */
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

    /**
     * Changes markup rate for a specified product.
     * @param id The ID of the product whose markup rate is to be changed.
     * @param markupRate The new markup rate to be applied.
     * */
    public static void changeMarkupRate(int id, int markupRate) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(STOCK)
                .set(STOCK.MARKUP_RATE, markupRate)
                .where(STOCK.ITEM_ID.eq(id))
                .execute();
    }

    /**
     * Used in sales within CA POS and PU, to deduct stock after a successful purchase.
     * @param productID The ID of the product to be deducted.
     * @param quantity The quantity of stock to be deducted.
     * @return HTTP status code in relation to rows affected. 200 if the stock was successfully deducted, 404 if the stock is insufficient.
     * */
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

    /**
     * Gets product name from the database based on product ID.
     * @param productID The ID of the product whose name is to be fetched.
     * */
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
