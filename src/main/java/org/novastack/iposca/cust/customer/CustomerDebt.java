package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.Customer;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.Date;
import static schema.tables.CustomerDebt.CUSTOMER_DEBT;

public class CustomerDebt {
    private int customerID;
    private float balance;
    private String status1Reminder;
    private Date date1Reminder;
    private String status2Reminder;
    private Date date2Reminder;
    private Date statusChangedAt;

    public CustomerDebt(int customerID, float balance, String status1Reminder, Date date1Reminder, String status2Reminder, Date date2Reminder, Date statusChangedAt) {
        this.customerID = customerID;
        this.balance = balance;
        this.status1Reminder = status1Reminder;
        this.date1Reminder = date1Reminder;
        this.status2Reminder = status2Reminder;
        this.date2Reminder = date2Reminder;
        this.statusChangedAt = statusChangedAt;
    }

    public CustomerDebt() {}

    public void addDebtAccount(int customerID, String status) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.CUST_ID, customerID)
                .set(CUSTOMER_DEBT.STATUS, status)
                .execute();
    }

    public void recordDebt(int id, float amount) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.BALANCE, CUSTOMER_DEBT.BALANCE.add(amount))
                .where(CUSTOMER_DEBT.CUST_ID.eq(id))
                .execute();
    }
}
