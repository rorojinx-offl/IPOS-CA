package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.jooq.TableField;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.CustomerDebtRecord;

import java.time.LocalDate;
import java.util.ArrayList;

import static schema.tables.CustomerDebt.CUSTOMER_DEBT;
import static schema.tables.Customer.CUSTOMER;

public class CustomerDebt {
    private int customerID;
    private String customerName;
    private float custCreditLimit;
    private String custStatus;
    private float balance;
    private String status1Reminder;
    private LocalDate date1Reminder;
    private String status2Reminder;
    private LocalDate date2Reminder;
    private LocalDate statusChangedAt;

    public CustomerDebt(int customerID, float balance, String status1Reminder, LocalDate date1Reminder, String status2Reminder, LocalDate date2Reminder, LocalDate statusChangedAt) {
        this.customerID = customerID;
        this.balance = balance;
        this.status1Reminder = status1Reminder;
        this.date1Reminder = date1Reminder;
        this.status2Reminder = status2Reminder;
        this.date2Reminder = date2Reminder;
        this.statusChangedAt = statusChangedAt;
    }

    public CustomerDebt(int customerID, String customerName, float credLim, String status, float balance, String status1Reminder, LocalDate date1Reminder, String status2Reminder, LocalDate date2Reminder, LocalDate statusChangedAt) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.custCreditLimit = credLim;
        this.custStatus = status;
        this.balance = balance;
        this.status1Reminder = status1Reminder;
        this.date1Reminder = date1Reminder;
        this.status2Reminder = status2Reminder;
        this.date2Reminder = date2Reminder;
        this.statusChangedAt = statusChangedAt;
    }

    public CustomerDebt(int customerID, float balance) {
        this.customerID = customerID;
        this.balance = balance;
    }

    public CustomerDebt() {}

    public void upsertDebt(CustomerDebt cd, float creditLimit) {
        DSLContext ctx = JooqConnection.getDSLContext();
        float initialBalance = creditLimit - cd.getBalance();
        ctx.insertInto(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.CUST_ID, cd.getCustomerID())
                .set(CUSTOMER_DEBT.BALANCE, initialBalance)
                .onConflict(CUSTOMER_DEBT.CUST_ID)
                .doUpdate()
                .set(CUSTOMER_DEBT.BALANCE, CUSTOMER_DEBT.BALANCE.minus(cd.getBalance()))
                .execute();
    }

    public static void setReminderDateStatus(int customerID, CustomerEnums.ReminderType type, String date, String status) {
        TableField<CustomerDebtRecord,String> colDate = null;
        TableField<CustomerDebtRecord,String> colStatus = null;

        switch (type) {
            case CustomerEnums.ReminderType.FIRST -> {
                colDate = CUSTOMER_DEBT.DATE_1_REMINDER;
                colStatus = CUSTOMER_DEBT.STATUS_1_REMINDER;
            }
            case CustomerEnums.ReminderType.SECOND -> {
                colDate = CUSTOMER_DEBT.DATE_2_REMINDER;
                colStatus = CUSTOMER_DEBT.STATUS_2_REMINDER;
            }
        }

        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER_DEBT)
                .set(colDate, date)
                .set(colStatus, status)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    public static CustomerDebt getDebtSimple(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .fetchOne(record -> new CustomerDebt(
                        CUSTOMER_DEBT.CUST_ID.getValue(record),
                        CUSTOMER_DEBT.BALANCE.getValue(record)
                ));
    }

    public static CustomerDebt getDebtFull(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .fetchOne(record -> new CustomerDebt(
                        CUSTOMER_DEBT.CUST_ID.getValue(record),
                        CUSTOMER_DEBT.BALANCE.getValue(record),
                        CUSTOMER_DEBT.STATUS_1_REMINDER.getValue(record),
                        parseDate(CUSTOMER_DEBT.DATE_1_REMINDER.getValue(record)),
                        CUSTOMER_DEBT.STATUS_2_REMINDER.getValue(record),
                        parseDate(CUSTOMER_DEBT.DATE_2_REMINDER.getValue(record)),
                        parseDate(CUSTOMER_DEBT.STATUS_CHANGED_AT.getValue(record))
                ));
    }

    public static ArrayList<CustomerDebt> getAllDebts() {
        ArrayList<CustomerDebt> debts = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(CUSTOMER_DEBT).fetch().forEach(record -> {
            String custName = ctx.select(CUSTOMER.NAME)
                    .from(CUSTOMER_DEBT)
                    .join(CUSTOMER)
                    .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                    .fetchOne(CUSTOMER.NAME);

            Float creditLimitRetr = ctx.select(CUSTOMER.CREDITLIMIT)
                            .from(CUSTOMER_DEBT)
                                    .join(CUSTOMER)
                                            .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                                                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                    .fetchOneInto(Float.class);

            float creditLimit = creditLimitRetr == null ? 0f : creditLimitRetr;

            String custStatus = ctx.select(CUSTOMER.STATUS)
                            .from(CUSTOMER_DEBT)
                                    .join(CUSTOMER)
                                            .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                                                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                                                            .fetchOne(CUSTOMER.STATUS);

            debts.add(new CustomerDebt(
                    CUSTOMER_DEBT.CUST_ID.getValue(record),
                    custName,
                    creditLimit,
                    custStatus,
                    CUSTOMER_DEBT.BALANCE.getValue(record),
                    CUSTOMER_DEBT.STATUS_1_REMINDER.getValue(record),
                    parseDate(CUSTOMER_DEBT.DATE_1_REMINDER.getValue(record)),
                    CUSTOMER_DEBT.STATUS_2_REMINDER.getValue(record),
                    parseDate(CUSTOMER_DEBT.DATE_2_REMINDER.getValue(record)),
                    parseDate(CUSTOMER_DEBT.STATUS_CHANGED_AT.getValue(record))
            ));
        });
        return debts;
    }

    public static void dryDeleteDebt(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.BALANCE, 0f)
                .set(CUSTOMER_DEBT.STATUS_1_REMINDER, CustomerEnums.ReminderStatus.NO_NEED.name())
                .set(CUSTOMER_DEBT.DATE_1_REMINDER, (String) null)
                .set(CUSTOMER_DEBT.STATUS_2_REMINDER, CustomerEnums.ReminderStatus.NO_NEED.name())
                .set(CUSTOMER_DEBT.DATE_2_REMINDER, (String) null)
                .set(CUSTOMER_DEBT.STATUS_CHANGED_AT, (String) null)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    public static void deleteDebt(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    private static LocalDate parseDate(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    public int getCustomerID() {
        return customerID;
    }

    public float getBalance() {
        return balance;
    }

    public String getStatus1Reminder() {
        return status1Reminder;
    }

    public LocalDate getDate1Reminder() {
        return date1Reminder;
    }

    public String getStatus2Reminder() {
        return status2Reminder;
    }

    public LocalDate getDate2Reminder() {
        return date2Reminder;
    }

    public LocalDate getStatusChangedAt() {
        return statusChangedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public float getCustCreditLimit() {
        return custCreditLimit;
    }

    public String getCustStatus() {
        return custStatus;
    }
}
