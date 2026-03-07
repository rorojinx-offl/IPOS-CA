package org.novastack.iposca.cust;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.YearMonth;
import static schema.tables.CustomerMonthlySpend.CUSTOMER_MONTHLY_SPEND;

public class CustomerMonthlySpend {
    private int customerID;
    private String customerName;
    private YearMonth monthYear;
    private float spend;
    public record FlexiRateChange(boolean needChange, int rate) {}

    public CustomerMonthlySpend(int customerID, YearMonth monthYear, float spend) {
        this.customerID = customerID;
        this.monthYear = monthYear;
        this.spend = spend;
    }

    public CustomerMonthlySpend(int customerID, String customerName, YearMonth monthYear, float spend) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.monthYear = monthYear;
        this.spend = spend;
    }

    public CustomerMonthlySpend() {}

    public void recordSpend(CustomerMonthlySpend spend) {
        String monthYear = spend.getMonthYear().toString();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_MONTHLY_SPEND)
                .set(CUSTOMER_MONTHLY_SPEND.CUST_ID, spend.getCustomerID())
        .set(CUSTOMER_MONTHLY_SPEND.MONTH_YEAR, monthYear)
                .set(CUSTOMER_MONTHLY_SPEND.SPEND, spend.getSpend())
                .onConflict(CUSTOMER_MONTHLY_SPEND.CUST_ID, CUSTOMER_MONTHLY_SPEND.MONTH_YEAR)
                .doUpdate().set(CUSTOMER_MONTHLY_SPEND.SPEND, CUSTOMER_MONTHLY_SPEND.SPEND.plus(spend.getSpend())).execute();
    }

    public FlexiRateChange warrantsRateChange(int id) {
        String monthYear = YearMonth.now().toString();
        DSLContext ctx = JooqConnection.getDSLContext();
        float currSpending =  ctx.select(CUSTOMER_MONTHLY_SPEND.SPEND)
                .from(CUSTOMER_MONTHLY_SPEND)
                .where(CUSTOMER_MONTHLY_SPEND.CUST_ID.eq(id))
                .and(CUSTOMER_MONTHLY_SPEND.MONTH_YEAR.eq(monthYear))
                .fetchOneInto(Float.class);

        int newRate = calculateRate(currSpending);
        int currentRate = new FlexiDiscountPlan().getCurrentDiscountRate(id);

        if (newRate != currentRate) {
            return new FlexiRateChange(true, newRate);
        }

        return new FlexiRateChange(false, currentRate);
    }

    private int calculateRate(float currSpending) {
        if (currSpending >= 4000f) {return 50;}
        else if (currSpending >= 3000f) {return 30;}
        else if (currSpending >= 2000f) {return 20;}
        else if (currSpending >= 1000f) {return 10;}
        else return 0;
    }

    public int getCustomerID() {
        return customerID;
    }

    public YearMonth getMonthYear() {
        return monthYear;
    }

    public float getSpend() {
        return spend;
    }
}
