package org.novastack.iposca.cust.statement;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Map;

import static schema.tables.CustomerMonthlyBalance.CUSTOMER_MONTHLY_BALANCE;

/**
 * A utility class that handles and organises data required for generating monthly statements.
 * */
public class StatementService {
    /**
     * A record class that holds the required data for generating a monthly statement.
     * @param customer The customer information.
     * @param billingMonth The month of the billing cycle.
     * @param balance The balance due for the customer.
     * @param items The products bought with credit during the billing cycle.
     * */
    public record StatementInfo(Customer customer, YearMonth billingMonth, float balance, ArrayList<StatementItems> items) {}

    /**
     * Records the amount of debt accrued by a customer in a specific month. It is used by {@link org.novastack.iposca.cust.debt.DebtAutomationService}
     * at the end of each month.
     * @param custID The ID of the customer.
     * @param month The month of the billing cycle.
     * @param balance The balance due for the customer.
     * */
    public static void trackMonthlyDebt(int custID, YearMonth month, float balance) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_MONTHLY_BALANCE)
                .set(CUSTOMER_MONTHLY_BALANCE.CUST_ID, custID)
                .set(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR, month.toString())
                .set(CUSTOMER_MONTHLY_BALANCE.BALANCE_DUE, balance)
                .execute();
    }

    /**
     * When a debt gets absolved before the end of the month, there is no need for a statement, so delete the debt
     * tracking data.
     * @param custID The ID of the customer.
     * @param month The month of the billing cycle.
     * */
    public static void deleteMonthData(int custID, YearMonth month) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER_MONTHLY_BALANCE)
                .where(CUSTOMER_MONTHLY_BALANCE.CUST_ID.eq(custID))
                .and(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR.eq(month.toString()))
                .execute();
    }

    /**
     * Get eligible customers for generating statements.
     * @return An {@link ArrayList} of {@link Customer} objects.
     * */
    public static ArrayList<Customer> getEligibleCustomers() {
        DSLContext ctx = JooqConnection.getDSLContext();
        ArrayList<Customer> customers = new ArrayList<>();
        ctx.select(CUSTOMER_MONTHLY_BALANCE.CUST_ID)
                .from(CUSTOMER_MONTHLY_BALANCE)
                .where(CUSTOMER_MONTHLY_BALANCE.BALANCE_DUE.greaterThan(0f))
                .and(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR.eq(YearMonth.now().toString()))
                .fetch().forEach(record -> customers.add(new Customer().getCustomer(CUSTOMER_MONTHLY_BALANCE.CUST_ID.getValue(record))));

        return customers;
    }

    /**
     * A factory method that builds a {@link StatementInfo} object using data across several tables. First it gets
     * monthly balance data for the customer, then it gets the products bought with credit during the billing cycle. Then,
     * using {@link #convertSaleDataToStatementFormat(Map)} it flattens the sales data and converts into the
     * {@link StatementItems} bean format. This allows the statement to have a flat list of all items purchased with
     * credit during the billing cycle and tie them to a single customer. As a customer could've purchased multiple items
     * with credit during the billing cycle, we collect it as an {@link ArrayList} of {@link StatementItems}. Finally,
     * all the data collected is packaged into a {@link StatementInfo} object and returned.
     * @param custID The ID of the customer.
     * @param month The month of the billing cycle.
     * @return A {@link StatementInfo} object containing the required data for generating a monthly statement.
     * */
    public static StatementInfo buildStatementData(int custID, YearMonth month) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER_MONTHLY_BALANCE)
                .where(CUSTOMER_MONTHLY_BALANCE.CUST_ID.eq(custID))
                .and(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR.eq(month.toString()))
                .fetchOne(record -> {
                    Customer customer = new Customer().getCustomer(CUSTOMER_MONTHLY_BALANCE.CUST_ID.getValue(record));
                    YearMonth prevBillingMonth = YearMonth.parse(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR.getValue(record));
                    float balanceDue = CUSTOMER_MONTHLY_BALANCE.BALANCE_DUE.getValue(record);

                    Map<SaleService.Sale, ArrayList<SaleService.SaleItem>> saleData = SaleService.getSaleItems(CUSTOMER_MONTHLY_BALANCE.CUST_ID.getValue(record), prevBillingMonth);
                    if (saleData == null) {
                        System.out.println("Problem with Sale Data");
                        return null;
                    }
                    ArrayList<StatementItems> items = convertSaleDataToStatementFormat(saleData);

                    return new StatementInfo(
                            customer,
                            prevBillingMonth,
                            balanceDue,
                            items
                    );
                });
    }

    /**
     * Converts the sale data (which is in a {@link Map} format) into a {@link ArrayList} of {@link StatementItems}, which
     * can be used to simply list all the products bought with credit during the billing cycle and link it all to a single
     * customer. First, we iterate through the {@link Map}, which links a {@link org.novastack.iposca.sales.SaleService.Sale}
     * to the products (an {@link ArrayList} of {@link org.novastack.iposca.sales.SaleService.SaleItem}) it sold. Then,
     * we get the separate sale items from the sale object. Finally, we iterate through the sale items and add them to
     * the {@link ArrayList} of {@link StatementItems} whilst recording the sale date.
     * @param saleData A {@link Map} that links a {@link SaleService.Sale} to the products (an {@link ArrayList} of {@link SaleService.SaleItem})
     * @return An {@link ArrayList} of {@link StatementItems} containing all the products bought with credit during the billing cycle.
     * */
    private static ArrayList<StatementItems> convertSaleDataToStatementFormat(Map<SaleService.Sale, ArrayList<SaleService.SaleItem>> saleData) {
        ArrayList<StatementItems> items = new ArrayList<>();

        for (Map.Entry<SaleService.Sale, ArrayList<SaleService.SaleItem>> entry : saleData.entrySet()) {
            SaleService.Sale sale = entry.getKey();
            ArrayList<SaleService.SaleItem> saleItems = entry.getValue();
            LocalDate saleDate = sale.saleDateTime().toLocalDate();

            for (SaleService.SaleItem item : saleItems) {
                String productName = Stock.getProductName(item.productID());
                items.add(new StatementItems(productName, item.quantity(), item.price(), saleDate));
            }
        }
        return items;
    }
}
