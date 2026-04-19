package org.novastack.iposca.sales;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.exceptions.CreditPaymentException;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.SaleRecord;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerCharge.CUSTOMER_CHARGE;
import static schema.tables.CustomerDebt.CUSTOMER_DEBT;
import static schema.tables.CustomerMonthlySpend.CUSTOMER_MONTHLY_SPEND;
import static schema.tables.FlexiDsc.FLEXI_DSC;
import static schema.tables.Sale.SALE;
import static schema.tables.SaleItem.SALE_ITEM;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SaleTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private final List<Integer> saleIdsToCleanup = new ArrayList<>();
    private final List<Integer> customerIdsToCleanup = new ArrayList<>();

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
            ctx.deleteFrom(CUSTOMER_CHARGE).where(CUSTOMER_CHARGE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_MONTHLY_SPEND).where(CUSTOMER_MONTHLY_SPEND.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER_DEBT).where(CUSTOMER_DEBT.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(FLEXI_DSC).where(FLEXI_DSC.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(SALE).where(SALE.CUST_ID.eq(customerId)).execute();
            ctx.deleteFrom(CUSTOMER).where(CUSTOMER.ID.eq(customerId)).execute();
        }
        customerIdsToCleanup.clear();
    }

    @Test
    @DisplayName("TC-01: Test sale can be recorded for an account holder")
    @Order(1)
    void testSaleRecordForAccountHolder() {
        Customer customer = createCustomer(300f, CustomerEnums.DiscountPlan.FIXED, CustomerEnums.AccountStatus.NORMAL);
        SaleService.Sale sale = new SaleService.Sale(
                null,
                customer.getCustomerID(),
                CustomerEnums.PaymentMethod.CASH.name(),
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                15f
        );

        int saleId = SaleService.recordSale(sale);
        saleIdsToCleanup.add(saleId);

        SaleRecord stored = fetchSaleRecord(saleId);
        assertNotNull(stored);
        assertEquals(customer.getCustomerID(), stored.getCustId());
        assertEquals(15f, stored.getAmount(), 0.001f);
    }

    @Test
    @DisplayName("TC-02: Test card payment for an account holder")
    @Order(2)
    void testCardPaymentForAccountHolder() {
        Customer customer = createCustomer(300f, CustomerEnums.DiscountPlan.FIXED, CustomerEnums.AccountStatus.NORMAL);
        YearMonth exp = YearMonth.now().plusYears(1);
        SaleService.Sale sale = new SaleService.Sale(
                null,
                customer.getCustomerID(),
                CustomerEnums.PaymentMethod.CARD.name(),
                CustomerEnums.CardType.VISA.name(),
                "1111",
                "2222",
                exp,
                LocalDateTime.now(),
                10f
        );

        int saleId = SaleService.recordSale(sale);
        saleIdsToCleanup.add(saleId);

        SaleRecord stored = fetchSaleRecord(saleId);
        assertNotNull(stored);
        assertEquals(CustomerEnums.PaymentMethod.CARD.name(), stored.getPaymentMethod());
        assertEquals("1111", stored.getCardFirst_4());
        assertEquals("2222", stored.getCardLast_4());
        assertEquals(exp.toString(), stored.getCardExp());
    }

    @Test
    @DisplayName("TC-03: Test cash payment for occasional customer")
    @Order(3)
    void testCashPaymentForOccasionalCustomer() {
        SaleService.Sale guestCashSale = new SaleService.Sale(
                null,
                null,
                CustomerEnums.PaymentMethod.CASH.name(),
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                20f
        );

        int saleId = SaleService.recordSale(guestCashSale);
        saleIdsToCleanup.add(saleId);

        SaleRecord stored = fetchSaleRecord(saleId);
        assertNotNull(stored);
        assertNull(stored.getCustId());
        assertEquals(CustomerEnums.PaymentMethod.CASH.name(), stored.getPaymentMethod());
        assertEquals(20f, stored.getAmount(), 0.001f);
    }

    @Test
    @DisplayName("TC-04: Test card payment for occasional customer")
    @Order(4)
    void testCardPaymentForOccasionalCustomer() {
        SaleService.Sale guestCardSale = new SaleService.Sale(
                null,
                null,
                CustomerEnums.PaymentMethod.CARD.name(),
                CustomerEnums.CardType.MASTERCARD.name(),
                "3456",
                "7890",
                YearMonth.now().plusMonths(6),
                LocalDateTime.now(),
                20f
        );

        int saleId = SaleService.recordSale(guestCardSale);
        saleIdsToCleanup.add(saleId);

        SaleRecord stored = fetchSaleRecord(saleId);
        assertNotNull(stored);
        assertNull(stored.getCustId());
        assertEquals(CustomerEnums.PaymentMethod.CARD.name(), stored.getPaymentMethod());
    }

    @Test
    @DisplayName("TC-05: Test to see if sufficient credit can be used for payment")
    @Order(5)
    void testCreditPaymentWithSufficientCredit() throws CreditPaymentException {
        Customer customer = createCustomer(100f, CustomerEnums.DiscountPlan.FIXED, CustomerEnums.AccountStatus.NORMAL);
        SaleService.SaleDraft draft = new SaleService.SaleDraft(customer.getCustomerID(), new ArrayList<>(), 30f, 30f, 30f);

        PaymentService.processCreditPayment(draft, customer);

        CustomerDebt debt = CustomerDebt.getDebtSimple(customer.getCustomerID());
        assertNotNull(debt);
        assertEquals(70f, debt.getBalance(), 0.001f);
    }

    @Test
    @DisplayName("TC-06: Test to see if insufficient credit can be used for payment")
    @Order(6)
    void testCreditPaymentWithInsufficientCredit() {
        Customer customer = createCustomer(20f, CustomerEnums.DiscountPlan.FIXED, CustomerEnums.AccountStatus.NORMAL);
        SaleService.SaleDraft draft = new SaleService.SaleDraft(customer.getCustomerID(), new ArrayList<>(), 50f, 50f, 50f);

        CreditPaymentException ex = assertThrows(CreditPaymentException.class,
                () -> PaymentService.processCreditPayment(draft, customer));

        assertEquals("Not enough credits to make this payment!", ex.getMessage());
        assertNull(CustomerDebt.getDebtSimple(customer.getCustomerID()));
    }

    private Customer createCustomer(float creditLimit, CustomerEnums.DiscountPlan discountPlan, CustomerEnums.AccountStatus accountStatus) {
        Customer creator = new Customer();
        String suffix = String.valueOf(System.nanoTime());
        Customer customer = new Customer(
                "Sales TC " + suffix,
                "sales_" + suffix + "@test.local",
                "Sales Test Address",
                "0700000" + suffix.substring(Math.max(0, suffix.length() - 4)),
                creditLimit,
                discountPlan.name(),
                accountStatus.name()
        );
        creator.addCustomer(customer);
        int id = creator.getLatestID();
        customer.setCustomerID(id);
        customerIdsToCleanup.add(id);
        return customer;
    }

    private SaleRecord fetchSaleRecord(int saleId) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(SALE)
                .where(SALE.ID.eq(saleId))
                .fetchOne();
    }
}
