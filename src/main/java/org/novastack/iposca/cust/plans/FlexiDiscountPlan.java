package org.novastack.iposca.cust.plans;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.YearMonth;
import java.util.ArrayList;

import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerMonthlySpend.CUSTOMER_MONTHLY_SPEND;
import static schema.tables.FlexiDsc.FLEXI_DSC;

public class FlexiDiscountPlan implements DiscountPlans {
    private final int customerID;
    private final String customerName;
    private final int discountRate;
    private final float custSpending;

    public FlexiDiscountPlan(int customerID, int discountRate, String customerName, float custSpending) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = customerName;
        this.custSpending = custSpending;
    }

    public FlexiDiscountPlan(int customerID, int discountRate) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = null;
        this.custSpending = 0;
    }

    public FlexiDiscountPlan() {
        this.customerID = 0;
        this.discountRate = 0;
        this.customerName = null;
        this.custSpending = 0;
    }

    @Override
    public void modifyRate(DiscountPlans flexiPlan) {
        if (flexiPlan instanceof FlexiDiscountPlan) {
            DSLContext ctx = JooqConnection.getDSLContext();
            ctx.update(FLEXI_DSC)
                    .set(FLEXI_DSC.RATE, flexiPlan.getDiscountRate())
                    .where(FLEXI_DSC.CUST_ID.eq(flexiPlan.getCustomerID()))
                    .execute();
        } else {
            throw new IllegalArgumentException("Invalid discount plan type");
        }
    }

    @Override
    public void addDiscount(DiscountPlans flexiPlan) {
        if (flexiPlan instanceof FlexiDiscountPlan) {
            DSLContext ctx = JooqConnection.getDSLContext();
            ctx.insertInto(FLEXI_DSC)
                    .set(FLEXI_DSC.CUST_ID, flexiPlan.getCustomerID())
                    .execute();
        } else {
            throw new IllegalArgumentException("Invalid discount plan type");
        }
    }

    @Override
    public void removeDiscount(int d) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(FLEXI_DSC)
                .where(FLEXI_DSC.CUST_ID.eq(d))
                .execute();
    }

    @Override
    public ArrayList<DiscountPlans> getAllDiscounts() {
        ArrayList<DiscountPlans> fdps = new ArrayList<>();
        String monthYear = YearMonth.now().toString();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(FLEXI_DSC).fetch().forEach(record -> {
            String custName = ctx.select(CUSTOMER.NAME)
                    .from(FLEXI_DSC)
                    .join(CUSTOMER)
                    .on(FLEXI_DSC.CUST_ID.eq(CUSTOMER.ID))
                    .where(FLEXI_DSC.CUST_ID.eq(FLEXI_DSC.CUST_ID.getValue(record)))
                    .fetchOne(CUSTOMER.NAME);

            Float custSpendingRetr = ctx.select(CUSTOMER_MONTHLY_SPEND.SPEND)
                            .from(CUSTOMER_MONTHLY_SPEND)
                                    .where(CUSTOMER_MONTHLY_SPEND.CUST_ID.eq(FLEXI_DSC.CUST_ID.getValue(record)))
                    .and(CUSTOMER_MONTHLY_SPEND.MONTH_YEAR.eq(monthYear))
                    .fetchOneInto(Float.class);

            float custSpending = custSpendingRetr == null ? 0f : custSpendingRetr;

            fdps.add(new FlexiDiscountPlan(
                    FLEXI_DSC.CUST_ID.getValue(record),
                    FLEXI_DSC.RATE.getValue(record),
                    custName,
                    custSpending));
        });
        return fdps;
    }

    @Override
    public int getCustomerID() {
        return customerID;
    }

    @Override
    public String getCustomerName() {
        return customerName;
    }

    @Override
    public int getDiscountRate() {
        return discountRate;
    }

    @Override
    public int getCurrentDiscountRate(int id) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.select(FLEXI_DSC.RATE)
                .from(FLEXI_DSC)
                .where(FLEXI_DSC.CUST_ID.eq(id))
                .fetchOneInto(Integer.class);
    }

    public float getCustSpending() {
        return custSpending;
    }
}
