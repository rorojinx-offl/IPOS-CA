package org.novastack.iposca.cust;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static schema.tables.FixedDsc.FIXED_DSC;
import static schema.tables.Customer.CUSTOMER;

public class FixedDiscountPlan implements DiscountPlans {
    private final int customerID;
    private final String customerName;
    private final int discountRate;

    public FixedDiscountPlan(int customerID, int discountRate, String customerName) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = customerName;
    }

    public FixedDiscountPlan(int customerID, int discountRate) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = null;
    }

    public FixedDiscountPlan() {customerID = 0; discountRate = 0; customerName = null;}

    @Override
    public int getCustomerID() {
        return customerID;
    }

    @Override
    public int getDiscountRate() {
        return discountRate;
    }

    @Override
    public void modifyRate(DiscountPlans fixedPlan) {
        if (fixedPlan instanceof FixedDiscountPlan) {
            DSLContext ctx = JooqConnection.getDSLContext();
            ctx.update(FIXED_DSC)
                    .set(FIXED_DSC.RATE, fixedPlan.getDiscountRate())
                    .where(FIXED_DSC.CUST_ID.eq(fixedPlan.getCustomerID()))
                    .execute();
        } else {
            throw new IllegalArgumentException("Invalid discount plan type");
        }
    }

    public static void modifyRateForAll(int newRate) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(FIXED_DSC)
                .set(FIXED_DSC.RATE, newRate)
                .execute();
    }

    public static int getCurrentDiscountRate(int id) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.select(FIXED_DSC.RATE)
                .from(FIXED_DSC)
                .where(FIXED_DSC.CUST_ID.eq(id))
                .fetchOneInto(Integer.class);
    }

    @Override
    public void addDiscount(DiscountPlans fixedPlan) {
        if (fixedPlan instanceof FixedDiscountPlan) {
            DSLContext ctx = JooqConnection.getDSLContext();
            ctx.insertInto(FIXED_DSC)
                    .set(FIXED_DSC.CUST_ID, fixedPlan.getCustomerID())
                    .set(FIXED_DSC.RATE, fixedPlan.getDiscountRate())
                    .execute();
        } else {
            throw new IllegalArgumentException("Invalid discount plan type");
        }
    }

    @Override
    public void removeDiscount(int dID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(FIXED_DSC)
                .where(FIXED_DSC.CUST_ID.eq(dID))
                .execute();
    }

    @Override
    public ArrayList<DiscountPlans> getAllDiscounts() throws DataAccessException {
        ArrayList<DiscountPlans> fdps = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(FIXED_DSC).fetch().forEach(record -> {
            String custName = ctx.select(CUSTOMER.NAME)
                    .from(FIXED_DSC)
                            .join(CUSTOMER)
                                    .on(FIXED_DSC.CUST_ID.eq(CUSTOMER.ID))
                                    .where(FIXED_DSC.CUST_ID.eq(FIXED_DSC.CUST_ID.getValue(record)))
                                            .fetchOne(CUSTOMER.NAME);

            fdps.add(new FixedDiscountPlan(
                    FIXED_DSC.CUST_ID.getValue(record),
                    FIXED_DSC.RATE.getValue(record),
                    custName));
        });
        return fdps;
    }
}
