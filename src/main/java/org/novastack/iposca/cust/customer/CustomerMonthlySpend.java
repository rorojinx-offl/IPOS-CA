package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.YearMonth;
import static schema.tables.CustomerMonthlySpend.CUSTOMER_MONTHLY_SPEND;

/**
 * Class that records monthly spend of customers and uses that data to also calculate whether a rate change is needed.
 * */
public class CustomerMonthlySpend {
    private int customerID;
    /** Month and year of the spend*/
    private YearMonth monthYear;
    private float spend;
    /**
     * This record stores the need for a rate change and the new rate and is returned by {@link #warrantsRateChange(int id)}
     * and used by {@link org.novastack.iposca.sales.SaleService} to adjust flexi rates accordingly.
     * @param needChange Whether a rate change is needed.
     * @param rate The new rate.
     * */
    public record FlexiRateChange(boolean needChange, int rate) {}

    /**
     * Constructor for CustomerMonthlySpend class.
     * @param customerID The ID of the customer.
     * @param monthYear The month and year of the spend.
     * @param spend The amount spent.
     * */
    public CustomerMonthlySpend(int customerID, YearMonth monthYear, float spend) {
        this.customerID = customerID;
        this.monthYear = monthYear;
        this.spend = spend;
    }

    /** Default constructor for CustomerMonthlySpend class.*/
    public CustomerMonthlySpend() {}

    /**
     * Upsert method that records a customer's monthly spend. If the current month and/or customer ID doesn't exist,
     * it creates a new record for spend. If the current month and/or customer ID exists, it adds the spend to the
     * existing record.
     * @param spend The spend to be recorded.
     * */
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

    /**
     * Checks if a customer's monthly spend warrants a rate change. It uses the {@link #calculateRate(float currSpending)}
     * method to determine the new rate. It is used by {@link org.novastack.iposca.sales.SaleService} to adjust flexi rates
     * following a purchase.
     * @param id The ID of the customer.
     * @return A {@link FlexiRateChange} object indicating whether a rate change is needed and the new rate.
     * */
    public FlexiRateChange warrantsRateChange(int id) {
        String monthYear = YearMonth.now().toString();
        DSLContext ctx = JooqConnection.getDSLContext();
        float currSpending = ctx.select(CUSTOMER_MONTHLY_SPEND.SPEND)
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

    /**
     * Helper method to calculate the new rate based on the current spending. If the customer's spending does not meet
     * any of the predefined thresholds, the rate is set to 0.
     * @param currSpending The current spending of the customer.
     * @return {@code int} The new rate.
     * */
    private int calculateRate(float currSpending) {
        if (currSpending >= 300f) {return 2;}
        else if (currSpending >= 100f) {return 1;}
        //else if (currSpending >= 2000f) {return 20;}
        //else if (currSpending >= 1000f) {return 10;}
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
