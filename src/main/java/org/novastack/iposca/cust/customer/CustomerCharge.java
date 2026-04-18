package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.CustomerCharge.CUSTOMER_CHARGE;

import java.time.LocalDate;

/**
 * Class that records charges made on customer membership credits.
 * */
public class CustomerCharge {
    private int custID;
    private float amount;
    private LocalDate date;

    /**
     * Constructor for CustomerCharge class.
     * @param custID The ID of the customer.
     * @param amount The amount of the transaction.
     * @param date The date of the transaction.
     * */
    public CustomerCharge(int custID, float amount, LocalDate date) {
        this.custID = custID;
        this.amount = amount;
        this.date = date;
    }

    /** Default constructor for CustomerCharge class. */
    public CustomerCharge() {}

    /**
     * Records a credit transaction made by an account-holding customer. Only used for credit transactions by
     * {@link org.novastack.iposca.sales.PaymentService}.
     * @param charge The charge to be recorded.
     * */
    public void recordCharge(CustomerCharge charge) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_CHARGE)
                .set(CUSTOMER_CHARGE.CUST_ID, charge.getCustID())
                .set(CUSTOMER_CHARGE.AMOUNT, charge.getAmount())
                .set(CUSTOMER_CHARGE.DATE, charge.getDate().toString())
                .execute();
    }

    public int getCustID() {
        return custID;
    }

    public float getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}
