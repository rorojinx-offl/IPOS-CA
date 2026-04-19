package org.novastack.iposca.rpt;

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
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerCharge;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.rpt.model.DebtChangeData;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.model.TurnoverData;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;
import org.novastack.iposca.utils.db.JooqConnection;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schema.tables.AppConfig.APP_CONFIG;
import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerCharge.CUSTOMER_CHARGE;
import static schema.tables.CustomerDebt.CUSTOMER_DEBT;
import static schema.tables.CustomerRepayment.CUSTOMER_REPAYMENT;
import static schema.tables.FlexiDsc.FLEXI_DSC;
import static schema.tables.Sale.SALE;
import static schema.tables.SaleItem.SALE_ITEM;
import static schema.tables.Stock.STOCK;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private final List<Integer> saleIdsToCleanup = new ArrayList<>();
    private final List<Integer> stockIdsToCleanup = new ArrayList<>();
    private final List<Integer> customerIdsToCleanup = new ArrayList<>();
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
        DSLContext ctx = JooqConnection.getDSLContext();

        for (Integer saleId : saleIdsToCleanup) {
            ctx.deleteFrom(SALE_ITEM).where(SALE_ITEM.SALE_ID.eq(saleId)).execute();
            ctx.deleteFrom(SALE).where(SALE.ID.eq(saleId)).execute();
        }
        saleIdsToCleanup.clear();

        for (Integer stockId : stockIdsToCleanup) {
            ctx.deleteFrom(STOCK).where(STOCK.ITEM_ID.eq(stockId)).execute();
        }
        stockIdsToCleanup.clear();

        for (Integer customerId : customerIdsToCleanup) {
            ctx.deleteFrom(CUSTOMER_CHARGE).where(CUSTOMER_CHARGE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_REPAYMENT).where(CUSTOMER_REPAYMENT.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_DEBT).where(CUSTOMER_DEBT.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(FLEXI_DSC).where(FLEXI_DSC.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(SALE).where(SALE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER).where(CUSTOMER.ID.eq(customerId)).execute();
        }
        customerIdsToCleanup.clear();

        restoreVatConfig();
    }

    @Test
    @DisplayName("TC-01: Manager can generate turnover report for selected period")
    @Order(1)
    void testTurnoverReportGeneration() throws Exception {
        LocalDateTime inRangeA = LocalDate.now().minusDays(2).atTime(10, 0);
        LocalDateTime inRangeB = LocalDate.now().minusDays(1).atTime(15, 30);
        LocalDateTime outOfRange = LocalDate.now().minusDays(30).atTime(9, 0);

        saleIdsToCleanup.add(recordSale(inRangeA, 15f));
        saleIdsToCleanup.add(recordSale(inRangeB, 25f));
        saleIdsToCleanup.add(recordSale(outOfRange, 999f));

        ReportService service = new ReportService("Manager TC01");
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = LocalDate.now();

        String log = captureStdout(() -> {
            try {
                TurnoverData data = service.getTurnoverData(start, end);

                assertEquals(start, data.getReportPeriodStart());
                assertEquals(end, data.getReportPeriodEnd());
                assertEquals("Manager TC01", data.getGeneratedBy());
                assertNotNull(data.getGeneratedTimestamp());
                assertEquals(2, data.getTotalSalesCount());
                assertEquals(40f, data.getTotalSalesAmount(), 0.001f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(log.contains("AUDIT: TURNOVER_REPORT generated by Manager TC01"));
    }

    @Test
    @DisplayName("TC-02: Manager can generate stock availability report with totals data")
    @Order(2)
    void testStockAvailabilityReportGeneration() throws Exception {
        backupVatConfig();
        setVatRate(20);

        int lowStockId = createStock("tc02_low", 10f, 50, 2, 5);
        int outStockId = createStock("tc02_out", 8f, 20, 0, 3);
        int healthyStockId = createStock("tc02_ok", 12f, 10, 15, 5);
        stockIdsToCleanup.add(lowStockId);
        stockIdsToCleanup.add(outStockId);
        stockIdsToCleanup.add(healthyStockId);

        ReportService service = new ReportService("Manager TC02");

        String lowLog = captureStdout(() -> {
            try {
                List<StockItem> lowStock = service.getStockData("LOW_STOCK", "");
                assertTrue(lowStock.stream().anyMatch(item -> item.getId() == lowStockId));
                assertTrue(lowStock.stream().noneMatch(item -> item.getId() == outStockId));
                assertTrue(lowStock.stream().noneMatch(item -> item.getId() == healthyStockId));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(lowLog.contains("AUDIT: STOCK_REPORT generated by Manager TC02"));

        List<StockItem> outStock = service.getStockData("OUT_OF_STOCK", "");
        assertTrue(outStock.stream().anyMatch(item -> item.getId() == outStockId));
        assertTrue(outStock.stream().noneMatch(item -> item.getId() == lowStockId));

        List<StockItem> searched = service.getStockData("ALL", "tc02_low");
        assertEquals(1, searched.size());
        StockItem row = searched.get(0);
        assertEquals(lowStockId, row.getId());
        assertTrue(row.isLowStock());
        assertEquals(15f, row.getRetailPrice(), 0.001f);
        assertEquals(30f, row.getTotalStockValue(), 0.001f);
        assertEquals(6f, row.getVatAmount(), 0.001f);
    }

    @Test
    @DisplayName("TC-03: Manager can generate debt change report with reconciliation data")
    @Order(3)
    void testDebtChangeReportGeneration() throws Exception {
        ReportService service = new ReportService("Manager TC03");
        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate end = LocalDate.now();
        DebtChangeData baseline = service.getDebtChangeData(start, end);

        int customerId = createCustomer("TC03 Customer");

        CustomerCharge charge = new CustomerCharge(customerId, 30f, LocalDate.now().minusDays(1));
        charge.recordCharge(charge);

        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_REPAYMENT)
                .set(CUSTOMER_REPAYMENT.CUST_ID, customerId)
                .set(CUSTOMER_REPAYMENT.AMOUNT, 10f)
                .set(CUSTOMER_REPAYMENT.DATE, LocalDate.now().minusDays(1).toString())
                .set(CUSTOMER_REPAYMENT.METHOD, CustomerEnums.PaymentMethod.CASH.name())
                .execute();

        String log = captureStdout(() -> {
            try {
                DebtChangeData data = service.getDebtChangeData(start, end);

                assertEquals(start, data.getStartDate());
                assertEquals(end, data.getEndDate());
                assertEquals("Manager TC03", data.getGeneratedBy());
                assertNotNull(data.getGeneratedTimestamp());

                assertEquals(baseline.getNewDebtAccrued() + 30f, data.getNewDebtAccrued(), 0.001f);
                assertEquals(baseline.getPaymentsReceived() + 10f, data.getPaymentsReceived(), 0.001f);
                assertEquals(baseline.getClosingAggregateDebt() + 20f, data.getClosingAggregateDebt(), 0.001f);
                assertEquals(
                        data.getOpeningAggregateDebt() + data.getNewDebtAccrued() - data.getPaymentsReceived(),
                        data.getClosingAggregateDebt(),
                        0.001f
                );

                assertEquals(baseline.getTotalCreditSalesCount() + 1, data.getTotalCreditSalesCount());
                assertEquals(baseline.getTotalPaymentsCount() + 1, data.getTotalPaymentsCount());
                assertTrue(data.getTotalDebtorsCount() >= baseline.getTotalDebtorsCount() + 1);
                assertFalse(data.getPdfRows().isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(log.contains("AUDIT: DEBT_REPORT generated by Manager TC03"));
    }

    @Test
    @DisplayName("TC-04: Access roles for reports package")
    @Order(4)
    void testRoleAccessForReportsPackage() {
        Session managerSession = new Session(new User("m1", "password123", UserEnums.UserRole.MANAGER, "Manager User", LocalDate.now()));
        Session adminSession = new Session(new User("a1", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now()));

        assertTrue(managerSession.hasAccess(UserEnums.UserRole.MANAGER, UserEnums.UserAccess.RPT));
        assertTrue(adminSession.hasAccess(UserEnums.UserRole.ADMIN, UserEnums.UserAccess.RPT));
    }

    private int recordSale(LocalDateTime when, float amount) {
        SaleService.Sale sale = new SaleService.Sale(
                null,
                null,
                CustomerEnums.PaymentMethod.CASH.name(),
                null,
                null,
                null,
                null,
                when,
                amount
        );
        return SaleService.recordSale(sale);
    }

    private int createStock(String prefix, float bulkCost, int markupRate, int quantity, int stockLimit) {
        String name = prefix + "_" + System.nanoTime();
        Stock stock = new Stock(
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
        stock.createItem(stock);

        DSLContext ctx = JooqConnection.getDSLContext();
        Integer stockId = ctx.select(STOCK.ITEM_ID)
                .from(STOCK)
                .where(STOCK.NAME.eq(name))
                .fetchOne(STOCK.ITEM_ID);
        if (stockId == null) {
            throw new IllegalStateException("Stock item was not created for name: " + name);
        }
        return stockId;
    }

    private int createCustomer(String prefix) {
        String suffix = String.valueOf(System.nanoTime());
        Customer creator = new Customer();
        Customer customer = new Customer(
                prefix + " " + suffix,
                "rpt_" + suffix + "@test.local",
                "Report Test Address",
                "0700000" + suffix.substring(Math.max(0, suffix.length() - 4)),
                250f,
                CustomerEnums.DiscountPlan.FIXED.name(),
                CustomerEnums.AccountStatus.NORMAL.name()
        );
        creator.addCustomer(customer);
        int id = creator.getLatestID();
        customerIdsToCleanup.add(id);
        return id;
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

    private void setVatRate(int rate) {
        AppConfig config = new AppConfig(AppConfig.ConfigKey.VAT, AppConfigAPI.encodeInt(rate));
        config.configure(config);
    }

    private String captureStdout(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(output);
        try {
            System.setOut(capture);
            runnable.run();
        } finally {
            capture.flush();
            System.setOut(originalOut);
        }
        return output.toString();
    }
}
