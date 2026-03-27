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

public class StatementService {
    public static void trackMonthlyDebt(int custID, YearMonth month, float balance) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_MONTHLY_BALANCE)
                .set(CUSTOMER_MONTHLY_BALANCE.CUST_ID, custID)
                .set(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR, month.toString())
                .set(CUSTOMER_MONTHLY_BALANCE.BALANCE_DUE, balance)
                .execute();
    }

    public static void deleteMonthData(int custID, YearMonth month) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER_MONTHLY_BALANCE)
                .where(CUSTOMER_MONTHLY_BALANCE.CUST_ID.eq(custID))
                .and(CUSTOMER_MONTHLY_BALANCE.MONTH_YEAR.eq(month.toString()))
                .execute();
    }

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
