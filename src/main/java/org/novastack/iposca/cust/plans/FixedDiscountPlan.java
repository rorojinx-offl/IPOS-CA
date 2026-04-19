package org.novastack.iposca.cust.plans;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static schema.tables.FixedDsc.FIXED_DSC;
import static schema.tables.Customer.CUSTOMER;

/**
 * Class that handles fixed discount plans and is the child class of {@link DiscountPlans}.
 * */
public class FixedDiscountPlan implements DiscountPlans {
    private final int customerID;
    private final String customerName;
    private final int discountRate;

    /**
     * Constructor for FixedDiscountPlan class used for fetching all fixed discount plans.
     * @param customerID The ID of the customer.
     * @param discountRate The discount rate of the customer.
     * @param customerName The name of the customer.
     * */
    public FixedDiscountPlan(int customerID, int discountRate, String customerName) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = customerName;
    }

    /**
     * Constructor for FixedDiscountPlan class used for adding a new fixed discount plan or editing an existing one.
     * @param customerID The ID of the customer.
     * @param discountRate The discount rate of the customer.
     * */
    public FixedDiscountPlan(int customerID, int discountRate) {
        this.customerID = customerID;
        this.discountRate = discountRate;
        this.customerName = null;
    }

    /** Default constructor for FixedDiscountPlan class.*/
    public FixedDiscountPlan() {customerID = 0; discountRate = 0; customerName = null;}

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

    /**
     * Modifies the discount rate of a fixed discount plan.
     * @param fixedPlan The fixed discount plan to modify.
     * */
    @Override
    public void modifyRate(DiscountPlans fixedPlan) {
        //As both discount plans share the same interface, we can use instanceof to check the type of the discount plan.
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

    /**
     * Sets a discount rate for all fixed discount plans.
     * @param newRate The new discount rate to set.
     * */
    public static void modifyRateForAll(int newRate) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(FIXED_DSC)
                .set(FIXED_DSC.RATE, newRate)
                .execute();
    }

    /**
     * Just fetches the current discount rate of a fixed discount plan.
     * @param id The ID of the customer whose discount rate is to be fetched.
     * @return The current discount rate of the customer.
     * */
    @Override
    public int getCurrentDiscountRate(int id) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.select(FIXED_DSC.RATE)
                .from(FIXED_DSC)
                .where(FIXED_DSC.CUST_ID.eq(id))
                .fetchOneInto(Integer.class);
    }

    /**
     * Adds a new fixed discount plan to the database.
     * */
    @Override
    public void addDiscount(DiscountPlans fixedPlan) {
        //As both discount plans share the same interface, we can use instanceof to check the type of the discount plan.
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

    /**
     * Deletes a discount plan and is used for when discount plans are changed. As the table's schema deletes on cascade,
     * this method isn't directly used for the removal of a customer.
     * */
    @Override
    public void removeDiscount(int dID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(FIXED_DSC)
                .where(FIXED_DSC.CUST_ID.eq(dID))
                .execute();
    }

    /**
     * Gets all fixed discount plans from the database.
     * @return An {@link ArrayList} of {@link FixedDiscountPlan} objects.
     * @throws DataAccessException If there is an error in the database operation.
     * */
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
