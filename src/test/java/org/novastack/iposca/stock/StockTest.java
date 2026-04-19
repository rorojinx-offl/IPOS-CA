package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schema.tables.AppConfig.APP_CONFIG;
import static schema.tables.Stock.STOCK;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StockTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";
    private final List<Integer> stockIdsToCleanup = new ArrayList<>();
    private byte[] vatBeforeTest;

    @RegisterExtension
    static TestWatcher testWatcher = new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
            System.out.println(GREEN + "SUCCESS: " + context.getDisplayName() + RESET);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            String reason = cause == null ? "Unknown error" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
            System.out.println(RED + "ERROR: " + context.getDisplayName() + " -> " + reason + RESET);
        }
    };

    @AfterEach
    void cleanup() {
        for (Integer stockId : stockIdsToCleanup) {
            Stock.deleteItem(stockId);
        }
        stockIdsToCleanup.clear();
        restoreVatConfig();
    }

    @Test
    @DisplayName("TC-01: retail price is derived from bulk cost plus markup")
    @Order(1)
    void retailPriceFromMarkup() {
        String name = uniqueName("tc01");
        createStock(name, 10.0f, 50, 10, 2);

        Stock saleView = findSaleViewByName(name);
        assertEquals(15.0f, saleView.getBulkCost(), 0.001f);
    }

    @Test
    @DisplayName("TC-02: report total includes VAT at configured rate")
    @Order(2)
    void vatIsAppliedToReportedTotal() throws Exception {
        backupVatConfig();
        setVatRate(20);

        String itemName = uniqueName("tc02_item");
        Stock item = new Stock(
                itemName,
                StockEnums.ProductType.NON_IPOS.name(),
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                1,
                10.00f,
                50,
                1,
                1
        );
        item.createItem(item);
        stockIdsToCleanup.add(findStockIdByName(itemName));

        StockItem reportItem = findReportItemByName(itemName);
        float finalPrice = reportItem.getTotalStockValue() + reportItem.getVatAmount();

        assertEquals(18.0f, finalPrice, 0.001f);
        assertEquals(20.0f, reportItem.getVatRate(), 0.001f);
    }

    @Test
    @DisplayName("TC-03: zero markup keeps sale price equal to bulk cost")
    @Order(3)
    void zeroMarkupLeavesPriceUnchanged() {
        String name = uniqueName("tc03");
        createStock(name, 12.0f, 0, 4, 2);

        assertEquals(12.0f, findSaleViewByName(name).getBulkCost(), 0.001f);
    }

    @Test
    @DisplayName("TC-04: with VAT at zero, final value does not change")
    @Order(4)
    void zeroVatMeansNoExtraCharge() throws Exception {
        backupVatConfig();
        setVatRate(0);

        String name = uniqueName("tc04");
        createStock(name, 20.0f, 0, 1, 1);

        StockItem reportItem = findReportItemByName(name);
        float finalPrice = reportItem.getTotalStockValue() + reportItem.getVatAmount();

        assertEquals(reportItem.getTotalStockValue(), finalPrice, 0.001f);
        assertEquals(0.0f, reportItem.getVatAmount(), 0.001f);
    }

    @Test
    @DisplayName("TC-05: applying markup pushes retail above bulk cost")
    @Order(5)
    void markupIncreasesRetail() {
        String itemName = uniqueName("tc05_item");
        Stock item = new Stock(
                itemName,
                StockEnums.ProductType.NON_IPOS.name(),
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                1,
                8.00f,
                25,
                10,
                2
        );
        item.createItem(item);
        stockIdsToCleanup.add(findStockIdByName(itemName));

        Stock saleView = findSaleViewByName(itemName);
        assertTrue(saleView.getBulkCost() > 8.0f);
    }

    @Test
    @DisplayName("TC-06: final value is higher than retail when VAT is present")
    @Order(6)
    void vatMakesFinalHigherThanRetail() throws Exception {
        backupVatConfig();
        setVatRate(20);

        String name = uniqueName("tc06");
        createStock(name, 30.0f, 0, 1, 1);

        StockItem reportItem = findReportItemByName(name);
        float finalPrice = reportItem.getTotalStockValue() + reportItem.getVatAmount();

        assertTrue(finalPrice > reportItem.getRetailPrice());
    }

    @Test
    @DisplayName("TC-07: low-stock list contains only items at or under limit")
    @Order(7)
    void lowStockFilter() {
        String lowName = uniqueName("tc07_low");
        createStock(lowName, 5.0f, 10, 2, 5);

        String healthyName = uniqueName("tc07_healthy");
        createStock(healthyName, 5.0f, 10, 10, 5);

        List<Stock> lowStockList = Stock.getLowStock();

        assertTrue(lowStockList.stream().anyMatch(s -> s.getName().equals(lowName)));
        assertTrue(lowStockList.stream().noneMatch(s -> s.getName().equals(healthyName)));
    }

    private int findStockIdByName(String name) {
        DSLContext ctx = JooqConnection.getDSLContext();
        Integer id = ctx.select(STOCK.ITEM_ID)
                .from(STOCK)
                .where(STOCK.NAME.eq(name))
                .fetchOne(STOCK.ITEM_ID);
        if (id == null) {
            throw new IllegalStateException("Stock item was not created for name: " + name);
        }
        return id;
    }

    private Stock findSaleViewByName(String name) {
        return Stock.getAllStockForSale().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stock item not found in sale view: " + name));
    }

    private StockItem findReportItemByName(String name) throws Exception {
        ReportService reportService = new ReportService("StockTest");
        return reportService.getStockData("ALL", name).stream()
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stock item not found in report view: " + name));
    }

    private void createStock(String name, float bulkCost, int markupRate, int quantity, int stockLimit) {
        Stock item = new Stock(
                name,
                StockEnums.ProductType.NON_IPOS.name(),
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                1,
                bulkCost,
                markupRate,
                quantity,
                stockLimit
        );
        item.createItem(item);
        stockIdsToCleanup.add(findStockIdByName(name));
    }

    private void setVatRate(int rate) {
        AppConfig config = new AppConfig(AppConfig.ConfigKey.VAT, AppConfigAPI.encodeInt(rate));
        config.configure(config);
    }

    private void backupVatConfig() {
        vatBeforeTest = AppConfig.get(AppConfig.ConfigKey.VAT);
    }

    private void restoreVatConfig() {
        DSLContext ctx = JooqConnection.getDSLContext();
        if (vatBeforeTest == null) {
            ctx.deleteFrom(APP_CONFIG)
                    .where(APP_CONFIG.KEY.eq(AppConfig.ConfigKey.VAT.name()))
                    .execute();
            return;
        }

        AppConfig config = new AppConfig(AppConfig.ConfigKey.VAT, vatBeforeTest);
        config.configure(config);
    }

    private String uniqueName(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
