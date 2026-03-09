package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.CustomerCharge.CUSTOMER_CHARGE;

import java.time.LocalDate;

public class CustomerCharge {
    private int custID;
    private float amount;
    private LocalDate date;

    public CustomerCharge(int custID, float amount, LocalDate date) {
        this.custID = custID;
        this.amount = amount;
        this.date = date;
    }

    public CustomerCharge() {}

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
