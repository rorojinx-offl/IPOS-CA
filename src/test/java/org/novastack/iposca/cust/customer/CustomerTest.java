package org.novastack.iposca.cust.customer;

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
import org.novastack.iposca.cust.debt.DebtAutomationService;
import org.novastack.iposca.cust.plans.FixedDiscountPlan;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.cust.statement.StatementService;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerDebt.CUSTOMER_DEBT;
import static schema.tables.CustomerMonthlyBalance.CUSTOMER_MONTHLY_BALANCE;
import static schema.tables.CustomerMonthlySpend.CUSTOMER_MONTHLY_SPEND;
import static schema.tables.CustomerRepayment.CUSTOMER_REPAYMENT;
import static schema.tables.FixedDsc.FIXED_DSC;
import static schema.tables.FlexiDsc.FLEXI_DSC;
import static schema.tables.Sale.SALE;
import static schema.tables.SaleItem.SALE_ITEM;
import static schema.tables.Stock.STOCK;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private final List<Integer> customerIdsToCleanup = new ArrayList<>();
    private final List<Integer> stockIdsToCleanup = new ArrayList<>();
    private final List<Integer> saleIdsToCleanup = new ArrayList<>();

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

        for (Integer customerId : customerIdsToCleanup) {
            ctx.deleteFrom(CUSTOMER_REPAYMENT).where(CUSTOMER_REPAYMENT.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_MONTHLY_SPEND).where(CUSTOMER_MONTHLY_SPEND.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_MONTHLY_BALANCE).where(CUSTOMER_MONTHLY_BALANCE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_DEBT).where(CUSTOMER_DEBT.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(FIXED_DSC).where(FIXED_DSC.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(FLEXI_DSC).where(FLEXI_DSC.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(SALE).where(SALE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER).where(CUSTOMER.ID.eq(customerId)).execute();
        }
        customerIdsToCleanup.clear();

        for (Integer stockId : stockIdsToCleanup) {
            ctx.deleteFrom(STOCK).where(STOCK.ITEM_ID.eq(stockId)).execute();
        }
        stockIdsToCleanup.clear();
    }

    @Test
    @DisplayName("TC-01: can create a customer with normal status")
    @Order(1)
    void testCreateCustomerAccount() {
        String suffix = String.valueOf(System.nanoTime());
        String phone = "07" + suffix.substring(Math.max(0, suffix.length() - 9));
        Customer newCustomer = new Customer(
                "TC01 Customer " + suffix,
                "tc01_" + suffix + "@test.local",
                "10 Green Street, London",
                phone,
                500f,
                CustomerEnums.DiscountPlan.FIXED.name(),
                CustomerEnums.AccountStatus.NORMAL.name()
        );

        newCustomer.addCustomer(newCustomer);
        Customer stored = findCustomerByEmail(newCustomer.getEmail());
        assertNotNull(stored);
        customerIdsToCleanup.add(stored.getCustomerID());
        assertEquals(CustomerEnums.AccountStatus.NORMAL.name(), stored.getStatus());
    }

    @Test
    @DisplayName("TC-02: credit limit update is persisted")
    @Order(2)
    void testAssignCreditLimit() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FIXED, 100f, CustomerEnums.AccountStatus.NORMAL);
        Customer edited = new Customer(
                customer.getCustomerID(),
                customer.getName(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getPhone(),
                500f,
                customer.getDiscountPlan(),
                customer.getStatus()
        );

        new Customer().editCustomer(edited);
        Customer stored = new Customer().getCustomer(customer.getCustomerID());
        assertEquals(500f, stored.getCreditLimit(), 0.001f);
    }

    @Test
    @DisplayName("TC-03: fixed discount plan rate can be assigned")
    @Order(3)
    void testAssignFixedDiscountPlan() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FIXED, 300f, CustomerEnums.AccountStatus.NORMAL);
        FixedDiscountPlan plan = new FixedDiscountPlan(customer.getCustomerID(), 10);
        plan.addDiscount(plan);

        assertEquals(10, new FixedDiscountPlan().getCurrentDiscountRate(customer.getCustomerID()));
        assertFalse(hasFlexiPlan(customer.getCustomerID()));
    }

    @Test
    @DisplayName("TC-04: flexi rate changes when spend crosses threshold")
    @Order(4)
    void testAssignFlexibleDiscountPlan() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FLEXIBLE, 300f, CustomerEnums.AccountStatus.NORMAL);
        FlexiDiscountPlan plan = new FlexiDiscountPlan(customer.getCustomerID(), 0);
        plan.addDiscount(plan);

        CustomerMonthlySpend spend = new CustomerMonthlySpend(customer.getCustomerID(), YearMonth.now(), 150f);
        spend.recordSpend(spend);

        CustomerMonthlySpend.FlexiRateChange change = spend.warrantsRateChange(customer.getCustomerID());
        assertTrue(change.needChange());
        assertEquals(1, change.rate());

        FlexiDiscountPlan updatedPlan = new FlexiDiscountPlan(customer.getCustomerID(), change.rate());
        updatedPlan.modifyRate(updatedPlan);
        assertEquals(1, new FlexiDiscountPlan().getCurrentDiscountRate(customer.getCustomerID()));
        assertFalse(hasFixedPlan(customer.getCustomerID()));
    }

    @Test
    @DisplayName("TC-05: statement builder returns month balance with items")
    @Order(5)
    void testGenerateMonthlyStatementData() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FIXED, 400f, CustomerEnums.AccountStatus.NORMAL);

        String stockName = "TC05_Item_" + System.nanoTime();
        Stock item = new Stock(
                stockName,
                StockEnums.ProductType.NON_IPOS.name(),
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                1,
                5f,
                20,
                100,
                1
        );
        item.createItem(item);

        Integer stockId = JooqConnection.getDSLContext()
                .select(STOCK.ITEM_ID)
                .from(STOCK)
                .where(STOCK.NAME.eq(stockName))
                .fetchOneInto(Integer.class);
        if (stockId == null) {
            throw new IllegalStateException("Stock creation failed for " + stockName);
        }
        stockIdsToCleanup.add(stockId);

        YearMonth billingMonth = YearMonth.of(2099, 1);
        LocalDateTime saleTime = billingMonth.atDay(2).atTime(11, 0);

        int saleId = SaleService.recordSale(new SaleService.Sale(
                null,
                customer.getCustomerID(),
                CustomerEnums.PaymentMethod.CREDITS.name(),
                null,
                null,
                null,
                null,
                saleTime,
                40f
        ));
        saleIdsToCleanup.add(saleId);
        SaleService.recordSaleItem(new SaleService.SaleItem(null, saleId, stockId, 2, 20f, 40f));
        StatementService.trackMonthlyDebt(customer.getCustomerID(), billingMonth, 40f);

        StatementService.StatementInfo info = StatementService.buildStatementData(customer.getCustomerID(), billingMonth);
        assertNotNull(info);
        assertEquals(customer.getCustomerID(), info.customer().getCustomerID());
        assertEquals(40f, info.balance(), 0.001f);
        assertFalse(info.items().isEmpty());
    }

    @Test
    @DisplayName("TC-06: daily run moves account to suspended after missed first reminder window")
    @Order(6)
    void testSuspendAfterMissedPaymentDeadline() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FIXED, 300f, CustomerEnums.AccountStatus.NORMAL);
        LocalDate firstReminderSentDate = LocalDate.now().minusDays(20);

        insertDebtRow(
                customer.getCustomerID(),
                120f,
                CustomerEnums.ReminderStatus.SENT.name(),
                firstReminderSentDate.toString(),
                null,
                null
        );

        DebtAutomationService.runDailyDebtEvaluation();

        Customer stored = new Customer().getCustomer(customer.getCustomerID());
        CustomerDebt debt = CustomerDebt.getDebtFull(customer.getCustomerID());
        assertEquals(CustomerEnums.AccountStatus.SUSPENDED.name(), stored.getStatus());
        assertEquals(CustomerEnums.ReminderStatus.DUE.name(), debt.getStatus2Reminder());
        assertEquals(firstReminderSentDate.plusDays(15), debt.getDate2Reminder());
    }

    @Test
    @DisplayName("TC-07: cash repayment writes a record and clears debt")
    @Order(7)
    void testRecordCustomerPaymentAndClearDebt() {
        Customer customer = createCustomer(CustomerEnums.DiscountPlan.FIXED, 300f, CustomerEnums.AccountStatus.SUSPENDED);
        insertDebtRow(
                customer.getCustomerID(),
                80f,
                CustomerEnums.ReminderStatus.SENT.name(),
                LocalDate.now().minusDays(10).toString(),
                CustomerEnums.ReminderStatus.NO_NEED.name(),
                null
        );

        CustomerPayment payment = new CustomerPayment(
                customer.getCustomerID(),
                80f,
                LocalDate.now(),
                CustomerEnums.PaymentMethod.CASH.name()
        );
        payment.addRepayment(payment);

        Integer repayments = JooqConnection.getDSLContext()
                .selectCount()
                .from(CUSTOMER_REPAYMENT)
                .where(CUSTOMER_REPAYMENT.CUST_ID.eq(customer.getCustomerID()))
                .fetchOneInto(Integer.class);

        assertNotNull(repayments);
        assertTrue(repayments > 0);
        assertNull(CustomerDebt.getDebtSimple(customer.getCustomerID()));
    }

    private Customer createCustomer(CustomerEnums.DiscountPlan discountPlan, float creditLimit, CustomerEnums.AccountStatus status) {
        String suffix = String.valueOf(System.nanoTime());
        Customer customer = new Customer(
                "Cust " + suffix,
                "cust_" + suffix + "@test.local",
                "Address " + suffix,
                "07" + suffix.substring(Math.max(0, suffix.length() - 9)),
                creditLimit,
                discountPlan.name(),
                status.name()
        );
        customer.addCustomer(customer);

        Customer stored = findCustomerByEmail(customer.getEmail());
        if (stored == null) {
            throw new IllegalStateException("Customer creation failed");
        }

        customerIdsToCleanup.add(stored.getCustomerID());
        return stored;
    }

    private Customer findCustomerByEmail(String email) {
        Integer id = JooqConnection.getDSLContext()
                .select(CUSTOMER.ID)
                .from(CUSTOMER)
                .where(CUSTOMER.EMAIL.eq(email))
                .fetchOneInto(Integer.class);
        return id == null ? null : new Customer().getCustomer(id);
    }

    private boolean hasFixedPlan(int customerId) {
        Integer count = JooqConnection.getDSLContext()
                .selectCount()
                .from(FIXED_DSC)
                .where(FIXED_DSC.CUST_ID.eq(customerId))
                .fetchOneInto(Integer.class);
        return count != null && count > 0;
    }

    private boolean hasFlexiPlan(int customerId) {
        Integer count = JooqConnection.getDSLContext()
                .selectCount()
                .from(FLEXI_DSC)
                .where(FLEXI_DSC.CUST_ID.eq(customerId))
                .fetchOneInto(Integer.class);
        return count != null && count > 0;
    }

    private void insertDebtRow(int customerId, float balance, String status1, String date1, String status2, String date2) {
        JooqConnection.getDSLContext()
                .insertInto(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.CUST_ID, customerId)
                .set(CUSTOMER_DEBT.BALANCE, balance)
                .set(CUSTOMER_DEBT.STATUS_1_REMINDER, status1)
                .set(CUSTOMER_DEBT.DATE_1_REMINDER, date1)
                .set(CUSTOMER_DEBT.STATUS_2_REMINDER, status2)
                .set(CUSTOMER_DEBT.DATE_2_REMINDER, date2)
                .execute();
    }
}
